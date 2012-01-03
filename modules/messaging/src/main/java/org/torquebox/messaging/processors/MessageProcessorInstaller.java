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

package org.torquebox.messaging.processors;

import java.util.Hashtable;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.jmx.MBeanRegistrationService;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.jmx.ObjectNameFactory;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.hasingleton.HASingleton;
import org.projectodd.polyglot.messaging.BaseMessageProcessorGroup;
import org.projectodd.polyglot.messaging.destinations.DestinationUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.messaging.MessageProcessorGroup;
import org.torquebox.messaging.MessageProcessorGroupMBean;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.as.MessagingServices;

/**
 * <pre>
 * Stage: REAL
 *    In: MessageProcessorMetaData, EnvironmentMetaData
 *   Out: RubyMessageProcessor
 * </pre>
 * 
 */
public class MessageProcessorInstaller implements DeploymentUnitProcessor {

    public MessageProcessorInstaller() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );

        for (MessageProcessorMetaData each : allMetaData) {
            deploy( phaseContext, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, final MessageProcessorMetaData metaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        final String name = metaData.getName();

        ServiceName baseServiceName = MessagingServices.messageProcessor( unit, name );
        MessageProcessorGroup service = new MessageProcessorGroup( phaseContext.getServiceRegistry(), baseServiceName, metaData.getDestinationName() );
        service.setConcurrency( metaData.getConcurrency() );
        service.setDurable( metaData.isDurable() );
        service.setClientID( metaData.getClientID() );
        service.setMessageSelector( metaData.getMessageSelector() );
        service.setName( metaData.getName() );

        ServiceBuilder<BaseMessageProcessorGroup> builder = phaseContext.getServiceTarget().addService( baseServiceName, service )
                .addDependency( MessagingServices.messageProcessorComponentResolver( unit, name ), ComponentResolver.class, service.getComponentResolverInjector() )
                .addDependency( getConnectionFactoryServiceName(), ManagedReferenceFactory.class, service.getConnectionFactoryInjector() )
                .addDependency( getDestinationServiceName( metaData.getDestinationName() ), ManagedReferenceFactory.class, service.getDestinationInjector() )
                .addDependency( CoreServices.runtimePoolName( unit, "messaging" ), RubyRuntimePool.class, service.getRuntimePoolInjector() );

        if (metaData.isSingleton()) {
            builder.addDependency( HASingleton.serviceName( unit ) );
            builder.setInitialMode( Mode.PASSIVE );
        } else {
            builder.setInitialMode( Mode.ACTIVE );
        }

        builder.install();

        final RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        String mbeanName = ObjectNameFactory.create( "torquebox.messaging.processors", new Hashtable<String, String>() {
            {
                put( "app", rubyAppMetaData.getApplicationName() );
                put( "name", StringUtils.underscore( metaData.getName() ) );
            }
        } ).toString();

        MBeanRegistrationService<MessageProcessorGroupMBean> mbeanService = new MBeanRegistrationService<MessageProcessorGroupMBean>( mbeanName );
        phaseContext.getServiceTarget().addService( baseServiceName.append( "mbean" ), mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .addDependency( baseServiceName, MessageProcessorGroupMBean.class, mbeanService.getValueInjector() )
                .setInitialMode( Mode.PASSIVE )
                .install();
    }

    protected ServiceName getConnectionFactoryServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "ConnectionFactory" );
    }

    protected ServiceName getDestinationServiceName(String destination) {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( DestinationUtils.getServiceName( destination ) );
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
