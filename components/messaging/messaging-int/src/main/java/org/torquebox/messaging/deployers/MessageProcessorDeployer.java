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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.jmx.JMXUtils;
import org.torquebox.messaging.core.AbstractManagedDestination;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.core.ManagedTopic;
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
public class MessageProcessorDeployer extends AbstractDeployer {

    private String demand;

    public MessageProcessorDeployer() {
        setStage( DeploymentStages.REAL );
        addInput( MessageProcessorMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        addOutput( BeanMetaData.class );
        setRelativeOrder( 1000 );
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }

    public String getDemand() {
        return this.demand;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData( MessageProcessorMetaData.class );

        for (MessageProcessorMetaData each : allMetaData) {
            try {
                deploy( unit, each );
            } catch (NamingException e) {
                throw new DeploymentException( e );
            }
        }
    }

    protected void deploy(DeploymentUnit unit, MessageProcessorMetaData metaData) throws NamingException {

        String simpleName = metaData.getDestinationName() + "." + metaData.getRubyClassName();
        String beanName = AttachmentUtils.beanName( unit, RubyMessageProcessor.class, simpleName );

        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, RubyMessageProcessor.class.getName() );

        ValueMetaData runtimePoolInject = builder.createInject( AttachmentUtils.beanName( unit, RubyRuntimePool.class, "messaging" ) );

        builder.addPropertyMetaData( "name", metaData.getRubyClassName() );
        builder.addPropertyMetaData( "rubyRuntimePool", runtimePoolInject );
        builder.addPropertyMetaData( "messageSelector", metaData.getMessageSelector() );
        builder.addPropertyMetaData( "concurrency", metaData.getConcurrency() );
        builder.addPropertyMetaData( "componentResolver", createComponentResolver( unit, metaData ) );

        Class<? extends AbstractManagedDestination> demandClass = demandDestination( unit, metaData.getDestinationName() );

        if (demandClass != null) {
            String destinationBeanName = AttachmentUtils.beanName( unit, demandClass, metaData.getDestinationName() );
            builder.addDemand( destinationBeanName, ControllerState.CREATE, ControllerState.INSTALLED, null );
        }

        if (this.demand != null) {
            log.debug( "adding a demand for " + this.demand + " to " + simpleName );
            builder.addDemand( this.demand, ControllerState.CREATE, ControllerState.INSTALLED, null);
        }

        Context context = new InitialContext();

        ValueMetaData destinationJndiRef = builder.createInject( "naming:" + metaData.getDestinationName() );
        builder.addPropertyMetaData( "destination", destinationJndiRef );

        ValueMetaData connectionFactoryJndiRef = builder.createInject( "naming:/ConnectionFactory" );
        builder.addPropertyMetaData( "connectionFactory", connectionFactoryJndiRef );
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment(  RubyApplicationMetaData.class );
        
        String mbeanName = JMXUtils.jmxName( "torquebox.messaging.processors", rubyAppMetaData.getApplicationName() ).with( "name", StringUtils.underscore( metaData.getName() ) ).name();
        String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\""+ mbeanName + "\", exposedInterface=" + RubyMessageProcessorMBean.class.getName() + ".class)";
        builder.addAnnotation( jmxAnno ); 

        BeanMetaData beanMetaData = builder.getBeanMetaData();

        AttachmentUtils.attach( unit, beanMetaData );
    }

    protected Class<? extends AbstractManagedDestination> demandDestination(DeploymentUnit unit, String destinationName) {
        Set<? extends AbstractDestinationMetaData> destinations = unit.getAllMetaData( AbstractDestinationMetaData.class );

        for (AbstractDestinationMetaData each : destinations) {
            if (each.getName().equals( destinationName )) {
                if (each.getClass() == QueueMetaData.class) {
                    return ManagedQueue.class;
                } else {
                    return ManagedTopic.class;
                }
            }
        }
        return null;
    }

    protected RubyComponentResolver createComponentResolver(DeploymentUnit unit, MessageProcessorMetaData metaData) {
        RubyComponentResolver result = new RubyComponentResolver();
        result.setRubyClassName( metaData.getRubyClassName() );
        result.setRubyRequirePath( metaData.getRubyRequirePath() );
        result.setComponentName( "message-processor." + metaData.getRubyClassName() );
        result.setInitializeParams( metaData.getRubyConfig() );
        RubyApplicationMetaData envMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        if (envMetaData != null) {
            result.setAlwaysReload( envMetaData.isDevelopmentMode() );
            log.info( metaData.getRubyClassName() + " alwaysReload=" + envMetaData.isDevelopmentMode() );
        } else {
            log.warn( "No EnvironmentMetaData found for " + metaData.getRubyClassName() );
        }
        return result;
    }
}
