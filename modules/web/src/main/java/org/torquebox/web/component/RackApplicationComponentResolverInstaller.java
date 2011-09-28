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

package org.torquebox.web.component;

import java.io.IOException;
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
import org.projectodd.polyglot.core.as.DeploymentNotifier;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.component.ComponentEval;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.core.component.processors.BaseRubyComponentInstaller;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.rack.RackApplicationMetaData;

public class RackApplicationComponentResolverInstaller extends BaseRubyComponentInstaller {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null || rackAppMetaData == null) {
            return;
        }

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        ComponentEval instantiator = new ComponentEval();
        try {
            instantiator.setCode( getCode( rackAppMetaData.getRackUpScript( root ), rubyAppMetaData.getRoot() ) );
            instantiator.setLocation( rackAppMetaData.getRackUpScriptFile( root ).toURL().toString() );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        ServiceName serviceName = WebServices.rackApplicationComponentResolver( unit );
        ComponentResolver resolver = createComponentResolver( unit );
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( RackApplicationComponent.class );
        // Let Rack / Rails handle reloading for the web stack
        resolver.setAlwaysReload( false );
        
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.ON_DEMAND );
        addInjections( phaseContext, resolver, getInjectionPathPrefixes( phaseContext ), builder );
        addNamespaceContext( phaseContext, service, builder );
        builder.install();
        
        // Add to our notifier's watch list
        unit.addToAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY, serviceName );
    }
    

    protected List<String> getInjectionPathPrefixes(DeploymentPhaseContext phaseContext) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        
        List<String> prefixes = defaultInjectionPathPrefixes();
        prefixes.add(  rackAppMetaData.getRackUpScriptLocation() );
        prefixes.add( "app/controllers/" );
        prefixes.add( "app/helpers/" );
        prefixes.add( "." );
        
        return prefixes;
    }


    protected String getCode(String rackUpScript, VirtualFile root) {
        StringBuilder code = new StringBuilder();
        if (usesBundler( root )) {
            code.append( "require %q(bundler/setup)\n" );
        }
        code.append( "require %q(rack)\n" );
        code.append( "Rack::Builder.new{(\n" );
        code.append( rackUpScript );
        code.append( "\n)}.to_app" );
        return code.toString();
    }
    
    protected boolean usesBundler(VirtualFile root) {
        return root.getChild( "Gemfile" ).exists();
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.web.component" );
}
