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
        try {
            VirtualFile appKnobYml = getFile( unit );
            if (appKnobYml == null) {
                return;
            }

            TorqueBoxMetaData metaData = TorqueBoxYamlParsingProcessor.parse( appKnobYml );
            VirtualFile root = metaData.getApplicationRootFile();

            if (root == null) {
                throw new DeploymentUnitProcessingException( "No application root specified" );
            }

            if ( ! root.exists() ) {
                throw new DeploymentUnitProcessingException( "Application root does not exist: " + root.toURL().toExternalForm() );
            }

            ResourceRoot appRoot = new ResourceRoot( root, null );
            unit.putAttachment(Attachments.DEPLOYMENT_ROOT, appRoot);
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {}

    protected VirtualFile getFile(DeploymentUnit unit) throws DeploymentUnitProcessingException, IOException {
        List<VirtualFile> matches = new ArrayList<VirtualFile>();

        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        matches = root.getChildren( this.knobFilter );

        if (matches.size() > 1) {
            throw new DeploymentUnitProcessingException( "Multiple application yaml files found in " + root );
        }

        VirtualFile file = null;
        if (matches.size() == 1) {
            file = matches.get( 0 );
            if (file.getName().endsWith( "-rails.yml" )) {
                log.warn( "Usage of -rails.yml is deprecated, please rename to -knob.yml: " + file );
            } else if (file.getName().endsWith( "-rack.yml" )) {
                log.warn( "Usage of -rack.yml is deprecated, please rename to -knob.yml: " + file );
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


