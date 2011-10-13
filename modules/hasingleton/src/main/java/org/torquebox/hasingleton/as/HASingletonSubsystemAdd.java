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

package org.torquebox.hasingleton.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.torquebox.hasingleton.HASingleton;
import org.torquebox.hasingleton.HASingletonCoordinatorService;

public class HASingletonSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode subModel) {
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget );
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

        ServiceController<Void> singletonController = context.getServiceTarget().addService( HASingleton.serviceName(), new HASingleton() ).install();
        newControllers.add( singletonController );

        if (context.getServiceRegistry( false ).getService( ChannelFactoryService.getServiceName( null ) ) != null) {
            HASingletonCoordinatorService coordinator = new HASingletonCoordinatorService( singletonController, "ha-singleton" );
            newControllers.add(
                    context.getServiceTarget().addService( HASingleton.serviceName().append( "coordinator" ), coordinator )
                            .addDependency( ChannelFactoryService.getServiceName( null ), ChannelFactory.class, coordinator.getChannelFactoryInjector() )
                            .install()
                    );
        }
    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget) {
        // processorTarget.addDeploymentProcessor( Phase.PARSE, 31, new
        // StompYamlParsingProcessor() );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    static final HASingletonSubsystemAdd ADD_INSTANCE = new HASingletonSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.hasingleton.as" );

}
