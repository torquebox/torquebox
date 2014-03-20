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

package org.torquebox.stomp.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.projectodd.polyglot.stomp.processors.StompApplicationDefaultsProcessor;
import org.torquebox.stomp.component.processors.StompletComponentResolverInstaller;
import org.torquebox.stomp.processors.StompYamlParsingProcessor;
import org.torquebox.stomp.processors.StompletInstaller;
import org.torquebox.stomp.processors.StompletLoadPathProcessor;
import org.torquebox.stomp.processors.StompletsRuntimePoolProcessor;

public class StompSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode subModel) {
    }

    @Override
    protected void performBoottime(OperationContext context, final ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget );
            }
        }, OperationContext.Stage.RUNTIME );

    }

   
    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.PARSE, 31, new StompYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.PARSE, 1032, new StompApplicationDefaultsProcessor( true ) );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 0, new StompletLoadPathProcessor() );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 100, new StompletsRuntimePoolProcessor() );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, 5, new StompDependenciesProcessor() );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.POST_MODULE, 120, new StompletComponentResolverInstaller() );
        processorTarget.addDeploymentProcessor( StompExtension.SUBSYSTEM_NAME, Phase.INSTALL, 101, new StompletInstaller() );
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
