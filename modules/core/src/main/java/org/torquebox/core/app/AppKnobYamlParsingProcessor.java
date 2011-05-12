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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.SubDeploymentMarker;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;

public class AppKnobYamlParsingProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit(); 
        log.info("TOBY: called - phaseContext " + phaseContext );
        try {
            List<VirtualFile> files = getFiles( unit );

            for (VirtualFile appKnobYml : files) {

                TorqueBoxMetaData metaData = TorqueBoxYamlParsingProcessor.parse( appKnobYml );
                VirtualFile root = metaData.getApplicationRootFile();

                if (root == null) {
                    throw new DeploymentUnitProcessingException( "No application root specified" );
                }

                if ( ! root.exists() ) {
                    throw new DeploymentUnitProcessingException( "Application root does not exist: " + root.toURL().toExternalForm() );
                }

                RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
                rubyAppMetaData.setRoot( root );
                unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
                
                 ResourceRoot appRoot = new ResourceRoot( root, null );
                 SubDeploymentMarker.mark( appRoot );
                 unit.addToAttachmentList( Attachments.RESOURCE_ROOTS, appRoot );
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {}

    protected List<VirtualFile> getFiles(DeploymentUnit unit) throws IOException {
        List<VirtualFile> matches = new ArrayList<VirtualFile>();

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        log.info("TOBY: root is " + root );

        matches = root.getChildren( this.knobFilter );

        for (VirtualFile each : matches) {
            if (each.getName().endsWith( "-rails.yml" )) {
                log.warn( "Usage of -rails.yml is deprecated, please rename to -knob.yml: " + each );
            } else if (each.getName().endsWith( "-rack.yml" )) {
                log.warn( "Usage of -rack.yml is deprecated, please rename to -knob.yml: " + each );
            }
        }

        return matches;
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


