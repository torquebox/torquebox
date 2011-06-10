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

package org.torquebox.web.websockets.component;

import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.DeploymentNotifier;
import org.torquebox.core.component.BaseRubyComponentDeployer;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.web.rack.RackApplicationMetaData;
import org.torquebox.web.websockets.WebSocketMetaData;
import org.torquebox.web.websockets.WebSocketsServices;

public class WebSocketProcessorComponentResolverInstaller extends BaseRubyComponentDeployer {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null || rackAppMetaData == null) {
            return;
        }
        
        List<WebSocketMetaData> allMetaData = unit.getAttachmentList( WebSocketMetaData.ATTACHMENTS_KEY );
        
        for ( WebSocketMetaData each : allMetaData ) {
            deploy( phaseContext, each );
        }
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext, WebSocketMetaData webSocketMetaData) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null || rackAppMetaData == null) {
            return;
        }
        
        ComponentClass instantiator = new ComponentClass();
        instantiator.setClassName( webSocketMetaData.getRubyClassName() );
        instantiator.setRequirePath( webSocketMetaData.getRequirePath() );

        ServiceName serviceName = WebSocketsServices.webSocketProcessorComponentResolver( unit, webSocketMetaData.getName() );
        ComponentResolver resolver = createComponentResolver( unit );
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( WebSocketProcessorComponent.class );
        
        // Each connection should get a fresh component
        resolver.setAlwaysReload( true );

        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext, webSocketMetaData.getRequirePath() ), builder );
        builder.install();

        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
    }

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext, String requirePath) {

        final List<String> prefixes = defaultInjectionPathPrefixes();

        if (requirePath != null) {

            final DeploymentUnit unit = phaseContext.getDeploymentUnit();
            final ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
            final VirtualFile root = resourceRoot.getRoot();

            final String sourcePath = searchForSourceFile( root, requirePath, true, true, "app/websockets", "app/processors", "lib" );

            if (sourcePath != null) {
                prefixes.add( sourcePath );
            }
        }

        return prefixes;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.websockets.component" );
}
