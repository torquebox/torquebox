/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.projectodd.polyglot.core.processors.RootedDeploymentProcessor.rootSafe;

import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.connector.Connector;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.web.WebServer;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.projectodd.polyglot.web.WebConnectorConfigService;
import org.projectodd.polyglot.web.processors.VirtualHostInstaller;
import org.projectodd.polyglot.web.processors.WebApplicationDefaultsProcessor;
import org.torquebox.web.ModClusterUuidFixService;
import org.torquebox.web.component.processors.RackApplicationComponentResolverInstaller;
import org.torquebox.web.rack.processors.RackApplicationRecognizer;
import org.torquebox.web.rack.processors.RackRuntimeProcessor;
import org.torquebox.web.rack.processors.RackWebApplicationInstaller;
import org.torquebox.web.rack.processors.WebRuntimePoolProcessor;
import org.torquebox.web.rack.processors.WebYamlParsingProcessor;
import org.torquebox.web.rails.RailsRackProcessor;
import org.torquebox.web.rails.processors.RailsApplicationRecognizer;
import org.torquebox.web.rails.processors.RailsAutoloadPathProcessor;
import org.torquebox.web.rails.processors.RailsRuntimeProcessor;
import org.torquebox.web.rails.processors.RailsVersionProcessor;

class WebSubsystemAdd extends AbstractBoottimeAddStepHandler {

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.setEmptyObject();
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws OperationFailedException {

        context.addStep( new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                addDeploymentProcessors( processorTarget );
            }
        }, OperationContext.Stage.RUNTIME );
        

        try {
            addWebConnectorConfigServices( context, verificationHandler, newControllers );
            addModClusterUuidFixService( context, verificationHandler, newControllers );
        } catch (Exception e) {
            throw new OperationFailedException( e, null );
        }

    }

    protected void addDeploymentProcessors(final DeploymentProcessorTarget processorTarget) {
        processorTarget.addDeploymentProcessor( Phase.PARSE, 0, rootSafe( new RackApplicationRecognizer() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 10, rootSafe( new RailsApplicationRecognizer() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 30, rootSafe( new WebYamlParsingProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 40, rootSafe( new RailsVersionProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 50, rootSafe( new RailsRackProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 60, rootSafe( new WebApplicationDefaultsProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 70, rootSafe( new RackWebApplicationInstaller() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 1000, rootSafe( new RailsRuntimeProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.PARSE, 1100, rootSafe( new RackRuntimeProcessor() ) );

        processorTarget.addDeploymentProcessor( Phase.DEPENDENCIES, 1, rootSafe( new WebDependenciesProcessor() ) );

        processorTarget.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 100, rootSafe( new WebRuntimePoolProcessor() ) );
        processorTarget.addDeploymentProcessor( Phase.CONFIGURE_MODULE, 500, rootSafe( new RailsAutoloadPathProcessor() ) );

        processorTarget.addDeploymentProcessor( Phase.POST_MODULE, 120, rootSafe( new RackApplicationComponentResolverInstaller() ) );
        processorTarget.addDeploymentProcessor( Phase.INSTALL, 2100, rootSafe( new VirtualHostInstaller() ) );
    }

    protected void addWebConnectorConfigServices(final OperationContext context,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) throws Exception {
        for (Enumeration<?> e = System.getProperties().propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            Matcher matcher = maxThreadsPattern.matcher( key );
            if (matcher.matches()) {
                String connectorName = matcher.group( 1 );
                int maxThreads = Integer.parseInt( System.getProperty( key ) );
                addWebConnectorConfigService( context, verificationHandler, newControllers, connectorName, maxThreads );
            }
        }
    }

    protected void addWebConnectorConfigService(final OperationContext context,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers,
            String connectorName, int maxThreads) throws Exception {
        WebConnectorConfigService service = new WebConnectorConfigService();
        service.setMaxThreads( maxThreads );
        newControllers.add( context.getServiceTarget().addService( WebServices.WEB_CONNECTOR_CONFIG.append( connectorName ), service )
                .addDependency( WebSubsystemServices.JBOSS_WEB_CONNECTOR.append( connectorName ), Connector.class, service.getConnectorInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.ACTIVE )
                .install() );
    }

    protected void addModClusterUuidFixService(final OperationContext context,
            ServiceVerificationHandler verificationHandler,
            List<ServiceController<?>> newControllers) {
        ModClusterUuidFixService service = new ModClusterUuidFixService();
        newControllers.add( context.getServiceTarget().addService( WebServices.MOD_CLUSTER_UUID_FIX, service )
                .addDependency( WebSubsystemServices.JBOSS_WEB, WebServer.class, service.getWebServerInjector() )
                .addListener( verificationHandler )
                .setInitialMode( Mode.PASSIVE )
                .install() );
    }

    static ModelNode createOperation(ModelNode address) {
        final ModelNode subsystem = new ModelNode();
        subsystem.get( OP ).set( ADD );
        subsystem.get( OP_ADDR ).set( address );
        return subsystem;
    }

    static final WebSubsystemAdd ADD_INSTANCE = new WebSubsystemAdd();
    static final Logger log = Logger.getLogger( "org.torquebox.web.as" );
    static final Pattern maxThreadsPattern = Pattern.compile( "org\\.torquebox\\.web\\.(.+)\\.maxThreads" );

}
