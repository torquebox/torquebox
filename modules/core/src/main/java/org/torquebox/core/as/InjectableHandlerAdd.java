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

package org.torquebox.core.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.List;
import java.util.ServiceLoader;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.msc.service.ServiceController;
import org.torquebox.core.injection.analysis.InjectableHandler;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;

public class InjectableHandlerAdd extends AbstractAddStepHandler {

    public InjectableHandlerAdd() {
    }
    
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.get(  "attributes", "module" ).set( operation.get( "attributes", "module" ) );
    }
    
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                  ServiceVerificationHandler verificationHandler,
                                  List<ServiceController<?>> newControllers) throws OperationFailedException {
        
        InjectableHandlerRegistry registry = (InjectableHandlerRegistry) context.getServiceRegistry( true )
                .getRequiredService( CoreServices.INJECTABLE_HANDLER_REGISTRY ).getValue();
        
        try {
            for (InjectableHandler eachHandler : getInjectableHandlers( operation )) {
                registry.addInjectableHandler( eachHandler );
            }
        } catch (ModuleLoadException e) {
            log.error( "Unable to add injectable handlers to registry", e );
        }
        
    }
    
    protected ServiceLoader<InjectableHandler> getInjectableHandlers(ModelNode operation) throws ModuleLoadException {
        String handlerModuleIdentifierStr = operation.get( "attributes", "module" ).asString();
        ModuleIdentifier handlerModuleIdentifier = ModuleIdentifier.create( handlerModuleIdentifierStr );
        return Module.loadServiceFromCallerModuleLoader( handlerModuleIdentifier, InjectableHandler.class );
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
