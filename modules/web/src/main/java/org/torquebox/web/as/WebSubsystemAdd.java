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

package org.torquebox.web.as;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

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
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.web.VirtualHostInstaller;
import org.torquebox.web.component.RackApplicationComponentResolverInstaller;
import org.torquebox.web.rack.RackApplicationDefaultsProcessor;
import org.torquebox.web.rack.RackApplicationRecognizer;
import org.torquebox.web.rack.RackRuntimeProcessor;
import org.torquebox.web.rack.RackWebApplicationDeployer;
import org.torquebox.web.rack.WebRuntimePoolProcessor;
import org.torquebox.web.rack.WebYamlParsingProcessor;
import org.torquebox.web.rails.RailsApplicationRecognizer;
import org.torquebox.web.rails.RailsAutoloadPathProcessor;
import org.torquebox.web.rails.RailsRackProcessor;
import org.torquebox.web.rails.RailsRuntimeProcessor;
import org.torquebox.web.rails.RailsVersionProcessor;
import org.torquebox.web.websockets.URLRegistryInstaller;
import org.torquebox.web.websockets.WebSocketContextInstaller;
import org.torquebox.web.websockets.WebSocketsRuntimePoolProcessor;
import org.torquebox.web.websockets.WebSocketsServerService;
import org.torquebox.web.websockets.WebSocketsServices;
import org.torquebox.web.websockets.WebSocketsYamlParsingProcessor;
import org.torquebox.web.websockets.component.WebSocketProcessorComponentResolverInstaller;

class WebSubsystemAdd implements ModelAddOperationHandler, BootOperationHandler {

    /** {@inheritDoc} */
    @Override
    public OperationResult execute(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler) {
        final ModelNode subModel = context.getSubModel();
        subModel.setEmptyObject();
        
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

    protected void addDeploymentProcessors(final BootOperationContext context) {
        context.addDeploymentProcessor( Phase.PARSE, 0, new RackApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 10, new RailsApplicationRecognizer() );
        context.addDeploymentProcessor( Phase.PARSE, 30, new WebYamlParsingProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 40, new RailsVersionProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 50, new RailsRackProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 60, new RackApplicationDefaultsProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 70, new RackWebApplicationDeployer() );
        context.addDeploymentProcessor( Phase.PARSE, 1000, new RailsRuntimeProcessor() );
        context.addDeploymentProcessor( Phase.PARSE, 1100, new RackRuntimeProcessor() );
        
        context.addDeploymentProcessor( Phase.PARSE, 1200, new WebSocketsYamlParsingProcessor() );
        
        context.addDeploymentProcessor( Phase.DEPENDENCIES, 1, new WebDependenciesProcessor() );
        
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 100, new WebRuntimePoolProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 101, new WebSocketsRuntimePoolProcessor() );
        context.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 500, new RailsAutoloadPathProcessor() );
        
        context.addDeploymentProcessor( Phase.POST_MODULE, 120, new RackApplicationComponentResolverInstaller() );
        context.addDeploymentProcessor( Phase.POST_MODULE, 220, new WebSocketProcessorComponentResolverInstaller() );
        context.addDeploymentProcessor( Phase.INSTALL, 2100, new VirtualHostInstaller() );
        context.addDeploymentProcessor( Phase.INSTALL, 3100, new WebSocketContextInstaller( "localhost"  ) );
        context.addDeploymentProcessor( Phase.INSTALL, 4100, new URLRegistryInstaller() );
    }

    protected void addWebServices(RuntimeTaskContext context) {
        WebSocketsServerService service = new WebSocketsServerService();
        context.getServiceTarget().addService( WebSocketsServices.WEB_SOCKETS_SERVER, service )
            .setInitialMode( Mode.ON_DEMAND )
            .install();
    }
    
    protected RuntimeTask bootTask(final BootOperationContext bootContext, final ResultHandler resultHandler) {
        return new RuntimeTask() {
            @Override
            public void execute(RuntimeTaskContext context) throws OperationFailedException {
                addWebServices(context);
                addDeploymentProcessors( bootContext );
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

    static final WebSubsystemAdd ADD_INSTANCE = new WebSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.web.as" );

}
