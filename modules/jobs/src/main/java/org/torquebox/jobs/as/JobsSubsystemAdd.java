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

package org.torquebox.jobs.as;

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
import org.torquebox.jobs.processors.JobSchedulerInstaller;
import org.torquebox.jobs.processors.JobSchedulizerInstaller;
import org.torquebox.jobs.processors.JobsLoadPathProcessor;
import org.torquebox.jobs.processors.JobsRuntimePoolProcessor;
import org.torquebox.jobs.processors.JobsYamlParsingProcessor;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

public class JobsSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.setEmptyObject();
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
    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor( JobsExtension.SUBSYSTEM_NAME, Phase.PARSE, 30, new JobsYamlParsingProcessor() );
        processorTarget.addDeploymentProcessor( JobsExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 0, new JobsLoadPathProcessor() );
        processorTarget.addDeploymentProcessor( JobsExtension.SUBSYSTEM_NAME, Phase.CONFIGURE_MODULE, 100, new JobsRuntimePoolProcessor() );
        processorTarget.addDeploymentProcessor( JobsExtension.SUBSYSTEM_NAME, Phase.INSTALL, 0, new JobSchedulerInstaller() );
        processorTarget.addDeploymentProcessor( JobsExtension.SUBSYSTEM_NAME, Phase.INSTALL, 100, new JobSchedulizerInstaller() );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    static final JobsSubsystemAdd ADD_INSTANCE = new JobsSubsystemAdd();

    static final Logger log = Logger.getLogger( "org.torquebox.jobs.as" );

}
