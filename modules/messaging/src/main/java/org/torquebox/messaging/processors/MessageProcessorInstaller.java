/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

import java.util.ArrayList;
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
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.hasingleton.HASingleton;
import org.projectodd.polyglot.messaging.BaseMessageProcessorGroup;
import org.projectodd.polyglot.messaging.destinations.AbstractDestinationMetaData;
import org.projectodd.polyglot.messaging.destinations.DestinationUtils;
import org.projectodd.polyglot.messaging.destinations.QueueMetaData;
import org.projectodd.polyglot.messaging.destinations.TopicMetaData;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.messaging.MessageProcessorGroup;
import org.torquebox.messaging.MessageProcessorGroupMBean;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.RemoteMessageProcessorGroup;
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
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        List<MessageProcessorMetaData> allMetaData = unit.getAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY );

        for (MessageProcessorMetaData mpMetaData : allMetaData) {
            deploy( phaseContext, mpMetaData, findMetadataForDestination(unit, mpMetaData.getDestinationName()) );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, final MessageProcessorMetaData metaData, final AbstractDestinationMetaData destinationMetaData) throws DeploymentUnitProcessingException {

        if (destinationMetaData == null) {
            log.warn("Destination metadata couldn't be found, most likely something is wrong");
        }

        MessageProcessorGroup service;

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ServiceName baseServiceName = MessagingServices.messageProcessor( unit, metaData.getName() );

        log.debugf("Installing a message processor for destination '%s'", metaData.getDestinationName());

        if (destinationMetaData != null && destinationMetaData.isRemote()) {
            log.debug("Destination is located on a remote host");
            service = new RemoteMessageProcessorGroup( phaseContext.getServiceRegistry(), baseServiceName, metaData.getDestinationName(), destinationMetaData.getRemoteHost(), destinationMetaData.getUsername(), destinationMetaData.getPassword() );
        } else {
            service = new MessageProcessorGroup( phaseContext.getServiceRegistry(), baseServiceName, metaData.getDestinationName() );
        }

        service.setConcurrency( metaData.getConcurrency() );
        service.setDurable( metaData.isDurable() );
        service.setClientID( metaData.getClientID() );
        service.setMessageSelector( metaData.getMessageSelector() );
        service.setName( metaData.getName() );
        service.setXAEnabled( metaData.isXAEnabled() );
        service.setSynchronous( metaData.isSynchronous() );
        service.setStoppedAfterDeploy( metaData.isStopped() );

        ServiceBuilder<BaseMessageProcessorGroup> builder = phaseContext.getServiceTarget().addService( baseServiceName, service )
                .addDependency( MessagingServices.messageProcessorComponentResolver( unit, metaData.getName() ), ComponentResolver.class, service.getComponentResolverInjector() )
                .addDependency( CoreServices.runtimePoolName( unit, "messaging" ), RubyRuntimePool.class, service.getRuntimePoolInjector() );

        if (destinationMetaData == null || !destinationMetaData.isRemote()) {
            builder
                    .addDependency( getConnectionFactoryServiceName(), ManagedReferenceFactory.class, service.getConnectionFactoryInjector() )
                    .addDependency( getDestinationServiceName( metaData.getDestinationName() ), ManagedReferenceFactory.class, service.getDestinationInjector() );
        }

        if (metaData.isSingleton()) {
            builder.addDependency( HASingleton.serviceName( unit, "global" ) );
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

        ServiceName mbeanServiceName = baseServiceName.append( "mbean" );
        MBeanRegistrationService<MessageProcessorGroupMBean> mbeanService = new MBeanRegistrationService<MessageProcessorGroupMBean>( mbeanName, mbeanServiceName );
        phaseContext.getServiceTarget().addService( mbeanServiceName, mbeanService )
                .addDependency( DependencyType.OPTIONAL, MBeanServerService.SERVICE_NAME, MBeanServer.class, mbeanService.getMBeanServerInjector() )
                .addDependency( baseServiceName, MessageProcessorGroupMBean.class, mbeanService.getValueInjector() )
                .setInitialMode( Mode.PASSIVE )
                .install();
    }

    protected AbstractDestinationMetaData findMetadataForDestination(DeploymentUnit unit, String destination) {
        List<QueueMetaData> queuesMetaData = unit.getAttachmentList(QueueMetaData.ATTACHMENTS_KEY);
        List<TopicMetaData> topicsMetaData = unit.getAttachmentList(TopicMetaData.ATTACHMENTS_KEY);

        List<AbstractDestinationMetaData> metadatas = new ArrayList<AbstractDestinationMetaData>();

        if (queuesMetaData != null)
            metadatas.addAll(queuesMetaData);

        if (topicsMetaData != null)
            metadatas.addAll(topicsMetaData);

        // Let's find the destination metadata for specific name
        for (AbstractDestinationMetaData metaData : metadatas) {
            if (metaData.getName().equals(destination))
                return metaData;
        }

        // Shouldn't happen...
        return null;
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

    public static final Logger log = Logger.getLogger( "org.torquebox.messaging" );

}
