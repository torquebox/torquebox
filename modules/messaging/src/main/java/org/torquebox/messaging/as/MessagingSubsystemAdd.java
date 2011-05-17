package org.torquebox.messaging.as;

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
import org.torquebox.messaging.MessagingRuntimePoolDeployer;
import org.torquebox.messaging.MessagingYamlParsingProcessor;
import org.torquebox.messaging.QueueDeployer;
import org.torquebox.messaging.QueuesYamlParsingDeployer;
import org.torquebox.messaging.TopicDeployer;
import org.torquebox.messaging.TopicsYamlParsingDeployer;

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
         
         context.addDeploymentProcessor( Phase.PARSE, 10, new MessagingYamlParsingProcessor() );
         
         context.addDeploymentProcessor( Phase.PARSE, 11, new QueuesYamlParsingDeployer() );
         context.addDeploymentProcessor( Phase.PARSE, 12, new TopicsYamlParsingDeployer() );
         context.addDeploymentProcessor( Phase.PARSE, 13, new MessagingYamlParsingProcessor() );
         
         context.addDeploymentProcessor( Phase.DEPENDENCIES, 10, new MessagingDependenciesProcessor() );
         
         context.addDeploymentProcessor( Phase.POST_MODULE, 11, new QueueDeployer() );
         context.addDeploymentProcessor( Phase.POST_MODULE, 12, new TopicDeployer() );
         context.addDeploymentProcessor( Phase.POST_MODULE, 20, new MessagingRuntimePoolDeployer() );
    }

    protected void addMessagingServices(final RuntimeTaskContext context) {
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
