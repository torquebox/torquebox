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

package org.torquebox.cdi.as;

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
import org.torquebox.cdi.HackWeldBeanManagerServiceProcessor;

class CDISubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

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
        context.addDeploymentProcessor( Phase.STRUCTURE, 11, new CDIStructureProcessor() );
        context.addDeploymentProcessor( Phase.INSTALL, Phase.INSTALL_WELD_BEAN_MANAGER + 1, new HackWeldBeanManagerServiceProcessor() );
    }

    protected void addCDIServices(final RuntimeTaskContext context) {
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                addDeploymentProcessors( bootContext );
                addCDIServices( context );
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

    public CDISubsystemAdd() {
    }

    static final CDISubsystemAdd ADD_INSTANCE = new CDISubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.messaging.as" );

}
