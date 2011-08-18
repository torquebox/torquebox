/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.messaging.deployers;

import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.common.util.StringUtils;
import org.torquebox.common.util.JNDIUtils;
import org.torquebox.injection.AbstractRubyComponentDeployer;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.jmx.JMXUtils;
import org.torquebox.messaging.core.AbstractDestination;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.core.ManagedTopic;
import org.torquebox.messaging.core.RemoteQueue;
import org.torquebox.messaging.core.RemoteTopic;
import org.torquebox.messaging.core.RubyMessageProcessor;
import org.torquebox.messaging.core.RubyMessageProcessorMBean;
import org.torquebox.messaging.metadata.AbstractDestinationMetaData;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;

/**
 * <pre>
 * Stage: REAL
 *    In: MessageProcessorMetaData, EnvironmentMetaData
 *   Out: RubyMessageProcessor
 * </pre>
 * 
 */
public class MessageProcessorDeployer extends AbstractRubyComponentDeployer {

    public MessageProcessorDeployer() {
        setStage( DeploymentStages.REAL );
        addInput( MessageProcessorMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        setRelativeOrder( 1000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData( MessageProcessorMetaData.class );

        for (MessageProcessorMetaData each : allMetaData) {
            deploy( unit, each );
        }
    }

    protected void deploy(DeploymentUnit unit, MessageProcessorMetaData metaData) throws DeploymentException {

        String simpleName = metaData.getDestinationName() + "." + metaData.getRubyClassName();
        String beanName = AttachmentUtils.beanName( unit, RubyMessageProcessor.class, simpleName );

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );

        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, RubyMessageProcessor.class.getName() );

        ValueMetaData runtimePoolInject = builder.createInject( AttachmentUtils.beanName( unit, RubyRuntimePool.class, "messaging" ) );

        builder.addPropertyMetaData( "name", metaData.getRubyClassName() );
        builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInject );
        builder.addPropertyMetaData( "messageSelector", metaData.getMessageSelector() );
        builder.addPropertyMetaData( "concurrency", metaData.getConcurrency() );
        builder.addPropertyMetaData( "durable", metaData.getDurable() );
        builder.addPropertyMetaData( "applicationName", rubyAppMetaData.getApplicationName() );
        
        BeanMetaData componentResolver = createComponentResolver( unit, "message-processor." + metaData.getName(), metaData.getRubyClassName(), metaData.getRubyRequirePath(), metaData.getRubyConfig() );
        builder.addPropertyMetaData( "componentResolver", builder.createInject( componentResolver.getName() ) );

        Class<? extends AbstractDestination> demandClass = demandDestination( unit, metaData.getDestinationName() );

        if (demandClass != null) {
            String destinationBeanName = AttachmentUtils.beanName( unit, demandClass, metaData.getDestinationName() );
            builder.addDemand( destinationBeanName, ControllerState.CREATE, ControllerState.INSTALLED, null );
        }

        String destRemoteHost = getRemoteHost( unit, metaData.getDestinationName() );

        String jndiName = (destRemoteHost != null ? rubyAppMetaData.getApplicationName() + "." : "") + metaData.getDestinationName();
        ValueMetaData destinationJndiRef = builder.createInject( "naming:" + jndiName );
        builder.addPropertyMetaData( "destination", destinationJndiRef );

        if (destRemoteHost != null) {
            try {
                // Look up the remote HornetQ's ConnectionFactory.
                InitialContext ic = JNDIUtils.getInitialContext(destRemoteHost);
                builder.addPropertyMetaData( "connectionFactory", ic.lookup("/ConnectionFactory") );
            } catch (NamingException ne) {
                throw new DeploymentException(ne);
            }
        } else {
            ValueMetaData connectionFactoryJndiRef = builder.createInject( "naming:/ConnectionFactory" );
            builder.addPropertyMetaData( "connectionFactory", connectionFactoryJndiRef );
        }

        String mbeanName = JMXUtils.jmxName( "torquebox.messaging.processors", rubyAppMetaData.getApplicationName() )
                .with( "name", StringUtils.underscore( metaData.getName() ) ).name();
        String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\"" + mbeanName + "\", exposedInterface=" + RubyMessageProcessorMBean.class.getName()
                + ".class)";
        builder.addAnnotation( jmxAnno );

        BeanMetaData beanMetaData = builder.getBeanMetaData();

        AttachmentUtils.attach( unit, beanMetaData );
    }

    protected Class<? extends AbstractDestination> demandDestination(DeploymentUnit unit, String destinationName) {
        Set<? extends AbstractDestinationMetaData> destinations = unit.getAllMetaData( AbstractDestinationMetaData.class );

        for (AbstractDestinationMetaData each : destinations) {
            if (each.getName().equals( destinationName )) {
                if (each.getClass() == QueueMetaData.class) {
                    if (each.getRemoteHost() == null || each.getRemoteHost().equals("")) {
                        return ManagedQueue.class;
                    } else {
                        return RemoteQueue.class;
                    }
                } else {
                    if (each.getRemoteHost() == null || each.getRemoteHost().equals("")) {
                        return ManagedTopic.class;
                    } else {
                        return RemoteTopic.class;
                    }
                }
            }
        }
        return null;
    }

    protected String getRemoteHost(DeploymentUnit unit, String destinationName) {
        Set<? extends AbstractDestinationMetaData> destinations = unit.getAllMetaData( AbstractDestinationMetaData.class );

        for (AbstractDestinationMetaData each : destinations) {
            if (each.getName().equals( destinationName )) {
                if(each.isRemote()) {
                    return each.getRemoteHost();
                }
            }
        }
        return null;
    }

}
