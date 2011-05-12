package org.torquebox.web.as;

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
import org.torquebox.web.rack.RackApplicationDefaultsProcessor;
import org.torquebox.web.rack.RackApplicationFactoryDeployer;
import org.torquebox.web.rack.RackApplicationPoolDeployer;
import org.torquebox.web.rack.RackApplicationRecognizer;
import org.torquebox.web.rack.RackRuntimeProcessor;
import org.torquebox.web.rack.RackWebApplicationDeployer;
import org.torquebox.web.rack.WebRuntimePoolProcessor;
import org.torquebox.web.rack.WebYamlParsingProcessor;
import org.torquebox.web.rails.RailsApplicationRecognizer;
import org.torquebox.web.rails.RailsRuntimeProcessor;

class WebSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

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
        
        context.addDeploymentProcessor( Phase.PARSE, 0, new RackApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 10, new RailsApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 30, new WebYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 40, new RackWebApplicationDeployer() );
        
        context.addDeploymentProcessor( Phase.DEPENDENCIES, 0, new WebDependenciesProcessor() );
        
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 0, new RailsRuntimeProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 10, new RackRuntimeProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 100, new WebRuntimePoolProcessor() );
        
        context.addDeploymentProcessor( Phase.POST_MODULE, 0, new RackApplicationDefaultsProcessor() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 110, new RackApplicationFactoryDeployer() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 120, new RackApplicationPoolDeployer() );
        
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

    static final WebSubsystemAdd ADD_INSTANCE = new WebSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.web.as" );

}