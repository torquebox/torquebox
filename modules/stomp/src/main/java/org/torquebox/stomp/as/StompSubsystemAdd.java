/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.stomp.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.projectodd.polyglot.core.processors.RootedDeploymentProcessor.rootSafe;

import java.util.List;

import javax.transaction.TransactionManager;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.txn.service.TxnServices;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.projectodd.stilts.stomplet.server.StompletServer;
import org.torquebox.stomp.StompWebAdjuster;
import org.torquebox.stomp.StompletServerService;
import org.torquebox.stomp.component.processors.StompletComponentResolverInstaller;
import org.torquebox.stomp.processors.SessionManagerInstaller;
import org.torquebox.stomp.processors.StompApplicationDefaultsProcessor;
import org.torquebox.stomp.processors.StompYamlParsingProcessor;
import org.torquebox.stomp.processors.StompletContainerInstaller;
import org.torquebox.stomp.processors.StompletInstaller;
import org.torquebox.stomp.processors.StompletLoadPathProcessor;
import org.torquebox.stomp.processors.StompletsRuntimePoolProcessor;

public class StompSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode subModel) {
        subModel.get( "socket-binding" ).set( operation.get( "socket-binding" ) );
    }

    @Override
    protected void performBoottime(OperationContext context, final ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                final String bindingRef = operation.require( "socket-binding" ).asString();
                addDeploymentProcessors( processorTarget, bindingRef );
            }
        }, OperationContext.Stage.RUNTIME );

        try {
            addCoreServices( context, operation, model, verificationHandler, newControllers );
        } catch (Exception e) {
            throw new OperationFailedException( e, null );
        }
    }

    protected void addCoreServices(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        addStompletServer( context, operation, model, verificationHandler, newControllers );
    }

    private void addStompletServer(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        StompletServer server = new StompletServer();
        StompletServerService service = new StompletServerService( server );

        final String bindingRef = operation.require( "socket-binding" ).asString();

        ServiceController<StompletServer> controller = context.getServiceTarget().addService( StompServices.SERVER, service )
                .addDependency( TxnServices.JBOSS_TXN_TRANSACTION_MANAGER, TransactionManager.class, service.getTransactionManagerInjector() )
                .addDependency( SocketBinding.JBOSS_BINDING_NAME.append( bindingRef ), SocketBinding.class, service.getBindingInjector() )
                .setInitialMode( Mode.ON_DEMAND )
                .addListener( verificationHandler )
                .install();

        newControllers.add( controller );

    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget, String socketBindingRef) {
        processorTarget.addDeploymentProcessor( Phase.PARSE, 31, rootSafe( new StompYamlParsingProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 1031, rootSafe( new StompWebAdjuster() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 1032, rootSafe( new StompApplicationDefaultsProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 0, rootSafe( new StompletLoadPathProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 100, rootSafe( new StompletsRuntimePoolProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.DEPENDENCIES, 5, rootSafe( new StompDependenciesProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.POST_MODULE, 120, rootSafe( new StompletComponentResolverInstaller() ) );
        processorTarget.addDeploymentProcessor( Phase.INSTALL, 99, rootSafe( new SessionManagerInstaller( "localhost" ) ) );
        processorTarget.addDeploymentProcessor( Phase.INSTALL, 100, rootSafe( new StompletContainerInstaller( socketBindingRef ) ) );
        processorTarget.addDeploymentProcessor( Phase.INSTALL, 101, rootSafe( new StompletInstaller() ) );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    static final StompSubsystemAdd ADD_INSTANCE = new StompSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.stomp.as" );

}
