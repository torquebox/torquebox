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

package org.torquebox.messaging.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import javax.jms.ConnectionFactory;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.BootOperationContext;
import org.jboss.as.server.BootOperationHandler;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.jndi.ManagedReferenceInjectableService;
import org.torquebox.messaging.ApplicationNamingContextBindingProcessor;
import org.torquebox.messaging.BackgroundablePresetsDeployer;
import org.torquebox.messaging.MessageProcessorDeployer;
import org.torquebox.messaging.MessagingLoadPathProcessor;
import org.torquebox.messaging.MessagingRuntimePoolDeployer;
import org.torquebox.messaging.MessagingYamlParsingProcessor;
import org.torquebox.messaging.QueueDeployer;
import org.torquebox.messaging.QueuesYamlParsingProcessor;
import org.torquebox.messaging.TasksDeployer;
import org.torquebox.messaging.TasksScanningDeployer;
import org.torquebox.messaging.TasksYamlParsingProcessor;
import org.torquebox.messaging.TopicDeployer;
import org.torquebox.messaging.TopicsYamlParsingProcessor;
import org.torquebox.messaging.component.MessageProcessorComponentResolverInstaller;
import org.torquebox.messaging.injection.RubyConnectionFactoryService;

class MessagingSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();

        if (context instanceof BootOperationContext) {
            handleBootContext( context, resultHandler );
        } else {
            resultHandler.handleResultComplete();
        }
        return compensatingResult( operation );
    }

    protected boolean handleBootContext(final OperationContext operationContext, final ResultHandler resultHandler) {

        if (!(operationContext instanceof BootOperationContext)) {
            return false;
        }

        final BootOperationContext context = (BootOperationContext) operationContext;

        context.getRuntimeContext().setRuntimeTask( bootTask( context, resultHandler ) );
        return true;
    }

    protected void addDeploymentProcessors(final BootOperationContext context) {

    	context.addDeploymentProcessor( Phase.PARSE, 10, new BackgroundablePresetsDeployer() );
        context.addDeploymentProcessor( Phase.PARSE, 11, new QueuesYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 12, new TopicsYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 13, new MessagingYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 40, new TasksYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 41, new TasksScanningDeployer() );

        context.addDeploymentProcessor( Phase.DEPENDENCIES, 3, new MessagingDependenciesProcessor() );
        
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 0, new MessagingLoadPathProcessor() );

        //context.addDeploymentProcessor( Phase.POST_MODULE, 7, new MessagingInjectablesProcessor() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 11, new ApplicationNamingContextBindingProcessor() );
        
        context.addDeploymentProcessor( Phase.POST_MODULE, 220, new TasksDeployer() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 320, new MessagingRuntimePoolDeployer() );

        context.addDeploymentProcessor( Phase.INSTALL, 120, new MessageProcessorComponentResolverInstaller() );
        context.addDeploymentProcessor( Phase.INSTALL, 220, new MessageProcessorDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 221, new QueueDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 222, new TopicDeployer() );
    }

    protected void addMessagingServices(final RuntimeTaskContext context) {
        addRubyConnectionFactory( context );
    }

    protected void addRubyConnectionFactory(final RuntimeTaskContext context) {

        ServiceName managedFactoryServiceName = MessagingServices.RUBY_CONNECTION_FACTORY.append( "manager" );

        ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
        context.getServiceTarget().addService( managedFactoryServiceName, managementService )
                .addDependency( getJMSConnectionFactoryServiceName(), ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
                .install();

        RubyConnectionFactoryService service = new RubyConnectionFactoryService();
        context.getServiceTarget().addService( MessagingServices.RUBY_CONNECTION_FACTORY, service )
                .addDependency( managedFactoryServiceName, ConnectionFactory.class, service.getConnectionFactoryInjector() )
                .setInitialMode( Mode.ON_DEMAND )
                .install();
    }
    
    protected ServiceName getJMSConnectionFactoryServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "ConnectionFactory" );
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                addDeploymentProcessors( bootContext );
                addMessagingServices( context );
                resultHandler.handleResultComplete();
            }
        };
    }

    protected BasicOperationResult compensatingResult(ModelNode operation) {
        final ModelNode compensatingOperation = new ModelNode();
        compensatingOperation.get( OP ).set( REMOVE );
        compensatingOperation.get( OP_ADDR ).set( operation.get( OP_ADDR ) );
        return new BasicOperationResult( compensatingOperation );
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
