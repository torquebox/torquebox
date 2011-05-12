package org.torquebox.core.as;

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
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.app.ApplicationYamlParsingProcessor;
import org.torquebox.core.app.AppKnobYamlParsingProcessor;

import org.torquebox.core.app.EnvironmentYamlParsingProcessor;
import org.torquebox.core.app.RubyApplicationRecognizer;
import org.torquebox.core.pool.RuntimePoolDeployer;
import org.torquebox.core.runtime.RubyRuntimeFactoryDeployer;

class CoreSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        log.info( "Adding subsystem: " + context );
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();
        
        if (!handleBootContext( context, resultHandler )) {
            log.info( "Signal complete on non-boot task." );
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

        context.addDeploymentProcessor( Phase.PARSE, 0, new RubyApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 5, new AppKnobYamlParsingProcessor() );        
        context.addDeploymentProcessor( Phase.PARSE, 10, new TorqueBoxYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 20, new ApplicationYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 30, new EnvironmentYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.DEPENDENCIES, 0, new TorqueBoxDependenciesProcessor() );
        context.addDeploymentProcessor( Phase.INSTALL, 0, new RubyRuntimeFactoryDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 10, new RuntimePoolDeployer() );
        
        log.info( "Added deployment processors" );
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                log.info( "Executing boot task" );
                addDeploymentProcessors( bootContext );
                resultHandler.handleResultComplete();
                log.info( "signally completeness" );
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

    static final CoreSubsystemAdd ADD_INSTANCE = new CoreSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
