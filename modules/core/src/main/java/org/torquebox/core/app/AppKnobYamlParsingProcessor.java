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

package org.torquebox.core.app;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.server.deployment.module.TempFileProviderService;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.util.DeprecationUtil;

public class AppKnobYamlParsingProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        TorqueBoxMetaData metaData = null;
        VirtualFile root = null;
        ResourceRoot appRoot = null;

        try {
            VirtualFile appKnobYml = getFile( unit );
            if (appKnobYml == null) {
                return;
            }
            metaData = TorqueBoxYamlParsingProcessor.parse( appKnobYml );
            root = metaData.getApplicationRootFile();
            
            if (root == null) {
                throw new DeploymentUnitProcessingException( "No application root specified" );
            }
            
            if ( ! root.exists() ) {
                throw new DeploymentUnitProcessingException( "Application root does not exist: " + root.toURL().toExternalForm() );
            }
            
            if (root.exists() && !root.isDirectory()) {
                // Expand the referenced root if it's not a directory (ie .knob archive)
                final Closeable closable = VFS.mountZipExpanded( root, root, TempFileProviderService.provider() );
                final MountHandle mountHandle = new MountHandle( closable );
                appRoot = new ResourceRoot( root, mountHandle );
            } else {
                appRoot = new ResourceRoot( root, null );
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

        unit.putAttachment( TorqueBoxMetaData.ATTACHMENT_KEY, metaData );

        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( unit.getName() );
        rubyAppMetaData.setRoot( root );
        rubyAppMetaData.setEnvironmentName( metaData.getApplicationEnvironment() );
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        
        unit.putAttachment( Attachments.DEPLOYMENT_ROOT, appRoot );
    }

    @Override
    public void undeploy(DeploymentUnit context) {}

    protected VirtualFile getFile(DeploymentUnit unit) throws DeploymentUnitProcessingException, IOException {
        List<VirtualFile> matches = new ArrayList<VirtualFile>();

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        if (this.knobFilter.accepts( root )) {
            return root;
        }

        matches = root.getChildren( this.knobFilter );

        if (matches.size() > 1) {
            throw new DeploymentUnitProcessingException( "Multiple application yaml files found in " + root );
        }

        VirtualFile file = null;
        if (matches.size() == 1) {
            file = matches.get( 0 );
            if (file.getName().endsWith( "-rails.yml" )) {
                DeprecationUtil.log( log, "Usage of -rails.yml is deprecated, please rename to -knob.yml: " + file );
            } else if (file.getName().endsWith( "-rack.yml" )) {
                DeprecationUtil.log( log, "Usage of -rack.yml is deprecated, please rename to -knob.yml: " + file );
            }
        }

        return file;
    }

    private VirtualFileFilter knobFilter = (new VirtualFileFilter() {
            public boolean accepts(VirtualFile file) {
                return file.getName().endsWith( "-knob.yml" ) ||
                    file.getName().endsWith( "-rails.yml" ) ||
                    file.getName().endsWith( "-rack.yml" );
            }
        });

    private static final Logger log = Logger.getLogger( "org.torquebox.core.app" );
}


