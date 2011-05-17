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

import java.util.List;
import java.util.Set;

import org.hibernate.validator.metadata.BeanMetaData;
import org.jboss.as.server.deployment.DeploymentException;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.messaging.AbstractDestinationMetaData;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.QueueMetaData;

/**
 * <pre>
 * Stage: REAL
 *    In: MessageProcessorMetaData, EnvironmentMetaData
 *   Out: RubyMessageProcessor
 * </pre>
 * 
 */
public class MessageProcessorDeployer implements DeploymentUnitProcessor {

    private String demand;

    public MessageProcessorDeployer() {
    }

    public void setDemand(String demand) {
        this.demand = demand;
    }

    public String getDemand() {
        return this.demand;
    }

    @Override
    public void deploy(DeploymentPhaseContext context) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = context.getDeploymentUnit();
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENT_KEY );

        for (MessageProcessorMetaData each : allMetaData) {
            deploy( unit, each );
        }
    }

    protected void deploy(DeploymentUnit unit, MessageProcessorMetaData metaData) throws DeploymentUnitProcessingException {

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

        Class<? extends AbstractManagedDestination> demandClass = demandDestination( unit, metaData.getDestinationName() );

        if (demandClass != null) {
            String destinationBeanName = AttachmentUtils.beanName( unit, demandClass, metaData.getDestinationName() );
            builder.addDemand( destinationBeanName, ControllerState.CREATE, ControllerState.INSTALLED, null );
        }

        if (this.demand != null) {
            builder.addDemand( this.demand, ControllerState.CREATE, ControllerState.INSTALLED, null );
        }

        ValueMetaData destinationJndiRef = builder.createInject( "naming:" + metaData.getDestinationName() );
        builder.addPropertyMetaData( "destination", destinationJndiRef );

        ValueMetaData connectionFactoryJndiRef = builder.createInject( "naming:/ConnectionFactory" );
        builder.addPropertyMetaData( "connectionFactory", connectionFactoryJndiRef );


        String mbeanName = JMXUtils.jmxName( "torquebox.messaging.processors", rubyAppMetaData.getApplicationName() )
                .with( "name", StringUtils.underscore( metaData.getName() ) ).name();
        String jmxAnno = "@org.jboss.aop.microcontainer.aspects.jmx.JMX(name=\"" + mbeanName + "\", exposedInterface=" + RubyMessageProcessorMBean.class.getName()
                + ".class)";
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

}
