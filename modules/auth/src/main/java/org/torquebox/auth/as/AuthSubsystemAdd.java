package org.torquebox.auth.as;

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
import org.torquebox.auth.AuthYamlParsingProcessor;

public class AuthSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler{

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        log.info( "Adding torquebox-auth subsystem: " + context );
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();
        
        if (!handleBootContext( context, resultHandler )) {
            resultHandler.handleResultComplete();
        }
        log.info( "Added torquebox-auth subsystem: " + context );
        return compensatingResult( operation );
    }

    protected boolean handleBootContext(final OperationContext operationContext, final ResultHandler resultHandler) {

        if (!(operationContext instanceof BootOperationContext)) {
            return false;
        }

        final BootOperationContext context = (BootOperationContext) operationContext;
        log.info( "Handling torquebox-auth boot context: " + context );

        context.getRuntimeContext().setRuntimeTask( bootTask( context, resultHandler ) );
        log.info( "Handled torquebox-auth boot context: " + context );
        return true;
    }

    protected void addDeploymentProcessors(final BootOperationContext context) {
        log.info( "Adding torquebox-auth deployment processors" );
        // TODO: Add AuthenticatorDeployer, AuthenticationPolicyDeployer and AuthDefaultsDeployer
        context.addDeploymentProcessor( Phase.PARSE, 0, new AuthYamlParsingProcessor() );
        log.info( "Added torquebox-auth deployment processors" );
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                log.info( "Executing torquebox-auth boot task" );
                addDeploymentProcessors( bootContext );
                resultHandler.handleResultComplete();
                log.info( "Executed torquebox-auth boot task" );
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

    static final AuthSubsystemAdd ADD_INSTANCE = new AuthSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.auth.as" );

}
