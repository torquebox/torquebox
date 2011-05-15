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

package org.torquebox.jobs.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.server.BootOperationContext;
import org.jboss.as.server.BootOperationHandler;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.torquebox.jobs.JobSchedulerDeployer;
import org.torquebox.jobs.JobsRuntimePoolProcessor;
import org.torquebox.jobs.JobsYamlParsingProcessor;
import org.torquebox.jobs.ScheduledJobDeployer;
import org.torquebox.jobs.component.JobsComponentResolverInstaller;

public class JobsSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        log.info( "Adding subsystem: " + context );
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();
        
        if (!handleBootContext( context, resultHandler )) {
            resultHandler.handleResultComplete();
        }
        log.info( "Added subsystem: " + context );
        return compensatingResult( operation );
    }
    
    protected boolean handleBootContext(final OperationContext operationContext, final ResultHandler resultHandler) {

        if (!(operationContext instanceof BootOperationContext)) {
            return false;
        }

        final BootOperationContext context = (BootOperationContext) operationContext;
        log.info( "Handling boot context: " + context );

        context.getRuntimeContext().setRuntimeTask( bootTask( context, resultHandler ) );
        log.info( "Handled boot context: " + context );
        return true;
    }
    
    protected void addDeploymentProcessors(final BootOperationContext context) {
        log.info( "Adding deployment processors" );
        
        context.addDeploymentProcessor( Phase.PARSE, 30, new JobsYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 100, new JobsRuntimePoolProcessor() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 120, new JobsComponentResolverInstaller() );
        context.addDeploymentProcessor( Phase.INSTALL, 0, new JobSchedulerDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 10, new ScheduledJobDeployer() );
        
        log.info( "Added deployment processors" );
    }
    
    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                log.info( "Executing boot task" );
                addDeploymentProcessors( bootContext );
                resultHandler.handleResultComplete();
                log.info( "Executed boot task" );
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
    
    static final JobsSubsystemAdd ADD_INSTANCE = new JobsSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.jobs.as" );

}
