package org.torquebox.core.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.ServiceLoader;

import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.torquebox.core.injection.analysis.InjectableHandler;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;

public class InjectableHandlerAdd implements ModelAddOperationHandler {

    public InjectableHandlerAdd() {
    }

    @Override
    public OperationResult execute(final OperationContext operationContext, final ModelNode operation, ResultHandler resultHandler) throws OperationFailedException {
        if (operationContext.getRuntimeContext() != null) {
            operationContext.getRuntimeContext().setRuntimeTask( new RuntimeTask() {
                @Override
                public void execute(final RuntimeTaskContext context) throws OperationFailedException {

                    InjectableHandlerRegistry registry = (InjectableHandlerRegistry) context.getServiceRegistry()
                            .getRequiredService( CoreServices.INJECTABLE_HANDLER_REGISTRY ).getValue();

                    String handlerModuleIdentifierStr = operation.get( "attributes", "module" ).asString();
                    ModuleIdentifier handlerModuleIdentifier = ModuleIdentifier.create( handlerModuleIdentifierStr );

                    try {
                        ServiceLoader<InjectableHandler> serviceLoader = Module.loadServiceFromCallerModuleLoader( handlerModuleIdentifier, InjectableHandler.class );
                        for (InjectableHandler eachHandler : serviceLoader) {
                            operationContext.getSubModel().get( eachHandler.getType() ).setEmptyObject();
                            registry.addInjectableHandler( eachHandler );
                        }
                    } catch (ModuleLoadException e) {
                        log.error(  "Unable to add injectable-handlers for: " + handlerModuleIdentifierStr, e );
                    }
                }
            } );
        }
        return null;
    }

    public static ModelNode createOperation(ModelNode address, String subsystemName, String moduleName) {

        ModelNode injectionAddress = address.clone().add( "injector", subsystemName );

        final ModelNode op = new ModelNode();
        op.get( OP_ADDR ).set( injectionAddress );
        op.get( OP ).set( "add" );
        op.get( "attributes", "module" ).set( moduleName );
        return op;
    }

    static final InjectableHandlerAdd ADD_INSTANCE = new InjectableHandlerAdd();
    private static final Logger log = Logger.getLogger( "org.torquebox.core.as" );

}
