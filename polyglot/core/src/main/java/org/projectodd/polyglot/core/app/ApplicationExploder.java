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

package org.projectodd.polyglot.core.app;

import java.io.File;
import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class ApplicationExploder implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ApplicationMetaData appMetaData = unit.getAttachment( ApplicationMetaData.ATTACHMENT_KEY );
        
        if (appMetaData == null) {
            return;
        }
        
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        try {
            VirtualFile explodedRoot = getExplodedApplication( root );
            if (!root.equals( explodedRoot )) {
                appMetaData.explode( explodedRoot );
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }
    
    /**
     * This method is a hack to make sure the WAR is fully exploded. Currently
     * this is only needed for WARs that come through the DeclaredStructure
     * deployer. This should be removed when the DeclaredStructure deployer
     * correctly support exploding WARs.
     */
    private VirtualFile getExplodedApplication(VirtualFile virtualFile) throws IOException {
        File physicalRoot = virtualFile.getPhysicalFile();
        virtualFile = VFS.getChild( physicalRoot.getAbsolutePath() );

        return virtualFile;
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
