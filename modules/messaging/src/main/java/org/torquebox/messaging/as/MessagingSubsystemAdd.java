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

package org.torquebox.messaging.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import javax.jms.ConnectionFactory;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.DuplicateServiceException;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.projectodd.polyglot.messaging.destinations.HornetQStartupPoolService;
import org.projectodd.polyglot.messaging.processors.ApplicationNamingContextBindingProcessor;
import org.torquebox.core.injection.jndi.ManagedReferenceInjectableService;
import org.torquebox.messaging.component.processors.MessageProcessorComponentResolverInstaller;
import org.torquebox.messaging.destinations.processors.QueuesYamlParsingProcessor;
import org.torquebox.messaging.destinations.processors.TopicsYamlParsingProcessor;
import org.torquebox.messaging.injection.RubyConnectionFactoryService;
import org.torquebox.messaging.injection.RubyXaConnectionFactoryService;
import org.torquebox.messaging.processors.BackgroundablePresetsProcessor;
import org.torquebox.messaging.processors.DestinationizerInstaller;
import org.torquebox.messaging.processors.MessageProcessorInstaller;
import org.torquebox.messaging.processors.MessagingLoadPathProcessor;
import org.torquebox.messaging.processors.MessagingRuntimePoolProcessor;
import org.torquebox.messaging.processors.MessagingYamlParsingProcessor;
import org.torquebox.messaging.tasks.processors.TasksInstaller;
import org.torquebox.messaging.tasks.processors.TasksScanningProcessor;
import org.torquebox.messaging.tasks.processors.TasksYamlParsingProcessor;

class MessagingSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.setEmptyObject();
    }

    @Override
    protected void performBoottime(final OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {
        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget, context.getServiceTarget() );
            }
        }, OperationContext.Stage.RUNTIME );

        addMessagingServices( context, verificationHandler, newControllers );
    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget, ServiceTarget globalTarget) {

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 10, new BackgroundablePresetsProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 11, new QueuesYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 12, new TopicsYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 35, new MessagingYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 40, new TasksYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.PARSE, 41, new TasksScanningProcessor() );

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, 3, new MessagingDependenciesProcessor() );

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 0, new MessagingLoadPathProcessor() );

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 11, new ApplicationNamingContextBindingProcessor() );

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 220, new TasksInstaller() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 320, new MessagingRuntimePoolProcessor() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 420, new DestinationizerInstaller(globalTarget) );

        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.INSTALL, 120, new MessageProcessorComponentResolverInstaller() );
        processorTarget.addDeploymentProcessor( MessagingExtension.SUBSYSTEM_NAME, Phase.INSTALL, 220, new MessageProcessorInstaller() );
    }

    protected void addMessagingServices(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        addRubyConnectionFactory( context, verificationHandler, newControllers );
        addRubyXaConnectionFactory( context, verificationHandler, newControllers );
        addHornetQStartupPoolService( context, verificationHandler, newControllers );
    }

    protected void addRubyConnectionFactory(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {

        ServiceName managedFactoryServiceName = MessagingServices.RUBY_CONNECTION_FACTORY.append( "manager" );

        ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
        newControllers.add( context.getServiceTarget().addService( managedFactoryServiceName, managementService )
                .addDependency( getJMSConnectionFactoryServiceName(), ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
                .addListener( verificationHandler )
                .install() );

        RubyConnectionFactoryService service = new RubyConnectionFactoryService();
        newControllers.add( context.getServiceTarget().addService( MessagingServices.RUBY_CONNECTION_FACTORY, service )
                .addDependency( managedFactoryServiceName, ConnectionFactory.class, service.getConnectionFactoryInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.ON_DEMAND )
                .install() );
    }

    protected void addRubyXaConnectionFactory(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {

        ServiceName managedFactoryServiceName = MessagingServices.RUBY_XA_CONNECTION_FACTORY.append( "manager" );

        ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
        newControllers.add( context.getServiceTarget().addService( managedFactoryServiceName, managementService )
                .addDependency( getJMSConnectionFactoryServiceName(), ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
                .addListener( verificationHandler )
                .install() );

        RubyXaConnectionFactoryService service = new RubyXaConnectionFactoryService();
        newControllers.add( context.getServiceTarget().addService( MessagingServices.RUBY_XA_CONNECTION_FACTORY, service )
                .addDependency( managedFactoryServiceName, HornetQConnectionFactory.class, service.getConnectionFactoryInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.ON_DEMAND )
                .install() );
    }

    protected void addHornetQStartupPoolService(final OperationContext context, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        final ServiceName hornetQServiceName = org.jboss.as.messaging.MessagingServices.getHornetQServiceName( "default" );
        final ServiceName serviceName = HornetQStartupPoolService.getServiceName( hornetQServiceName );
        HornetQStartupPoolService service = new HornetQStartupPoolService();
        try {
            newControllers.add( context.getServiceTarget().addService( serviceName, service )
                                .addListener( verificationHandler )
                                .setInitialMode( Mode.ON_DEMAND )
                                .install() );
        } catch (DuplicateServiceException ignored) {
            //can happen if overlaid with Immutant
        }
    }

    protected ServiceName getJMSConnectionFactoryServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "ConnectionFactory" );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    public MessagingSubsystemAdd() {
    }

    static final MessagingSubsystemAdd ADD_INSTANCE = new MessagingSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.messaging.as" );

}
