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
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.core.ArchiveDirectoryMountingProcessor;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.app.AppJarScanningProcessor;
import org.torquebox.core.app.AppKnobYamlParsingProcessor;
import org.torquebox.core.app.ApplicationYamlParsingProcessor;
import org.torquebox.core.app.EnvironmentYamlParsingProcessor;
import org.torquebox.core.app.PoolingYamlParsingProcessor;
import org.torquebox.core.app.RubyApplicationDefaultsProcessor;
import org.torquebox.core.app.RubyApplicationDeployer;
import org.torquebox.core.app.RubyApplicationExploder;
import org.torquebox.core.app.RubyApplicationRecognizer;
import org.torquebox.core.app.RubyYamlParsingProcessor;
import org.torquebox.core.injection.CorePredeterminedInjectableDeployer;
import org.torquebox.core.injection.PredeterminedInjectableProcessor;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;
import org.torquebox.core.injection.analysis.InjectionIndexingProcessor;
import org.torquebox.core.pool.RuntimePoolDeployer;
import org.torquebox.core.runtime.RubyRuntimeFactoryDeployer;

class CoreSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        final ModelNode subModel = context.getSubModel();
        subModel.get( "injector" ).setEmptyObject();

        if (!handleBootContext( context, resultHandler )) {
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

    protected void addDeploymentProcessors(final BootOperationContext context, final InjectableHandlerRegistry registry) {
        ServerEnvironment environment = context.getController().getServerEnvironment();
        context.addDeploymentProcessor( Phase.STRUCTURE, 0, new AKnobRootMountProcessor( environment ) );
        context.addDeploymentProcessor( Phase.STRUCTURE, 10, new KnobStructureProcessor() );
        context.addDeploymentProcessor( Phase.STRUCTURE, 20, new AppKnobYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.STRUCTURE, 100, new AppJarScanningProcessor() );

        context.addDeploymentProcessor( Phase.PARSE, 0, new RubyApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 10, new TorqueBoxYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 20, new ApplicationYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 30, new EnvironmentYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 35, new PoolingYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 36, new RubyYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 40, new RubyApplicationDefaultsProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 100, new RubyApplicationExploder() );
        context.addDeploymentProcessor( Phase.PARSE, 4000, new BaseRubyRuntimeDeployer() );

        context.addDeploymentProcessor( Phase.DEPENDENCIES, 0, new CoreDependenciesProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 1000, new PredeterminedInjectableProcessor( registry ) );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 1001, new CorePredeterminedInjectableDeployer() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 1100, new InjectionIndexingProcessor( registry ) );
        context.addDeploymentProcessor( Phase.POST_MODULE, 100, new ArchiveDirectoryMountingProcessor() );
        context.addDeploymentProcessor( Phase.INSTALL, 0, new RubyRuntimeFactoryDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 10, new RuntimePoolDeployer() );
        context.addDeploymentProcessor( Phase.INSTALL, 1000, new DeploymentNotifierInstaller() );
        context.addDeploymentProcessor( Phase.INSTALL, 9000, new RubyApplicationDeployer() );
    }

    protected void addCoreServices(final RuntimeTaskContext context, InjectableHandlerRegistry registry) {
        context.getServiceTarget().addService( CoreServices.INJECTABLE_HANDLER_REGISTRY, registry )
          .setInitialMode( Mode.PASSIVE )
          .install();
    }

    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                InjectableHandlerRegistry registry = new InjectableHandlerRegistry();
                addDeploymentProcessors( bootContext, registry );
                addCoreServices( context, registry );
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

    public CoreSubsystemAdd() {
    }

    static final CoreSubsystemAdd ADD_INSTANCE = new CoreSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
