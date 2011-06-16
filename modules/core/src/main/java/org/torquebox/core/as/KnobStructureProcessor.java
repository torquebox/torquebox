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

package org.torquebox.core.as;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.api.ServerDeploymentRepository;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

public class KnobStructureProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if (!unit.getName().endsWith( ".knob" )) {
            return;
        }
        
        KnobDeploymentMarker.applyMark( unit );

        // Until AS7-810 is implemented, we need to unmount and remount the root .knob
        // so it's mounted expanded
        remountExpanded( unit );
    }
    
    protected void remountExpanded(DeploymentUnit unit) throws DeploymentUnitProcessingException {
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        if (resourceRoot.getMountHandle() != null) {
            VFSUtils.safeClose( resourceRoot.getMountHandle() );
        }
        
        try {
            final ServerDeploymentRepository serverDeploymentRepository = unit.getAttachment(Attachments.SERVER_DEPLOYMENT_REPOSITORY);
            final Closeable closable = serverDeploymentRepository.mountDeploymentContent(unit.getAttachment( Attachments.DEPLOYMENT_CONTENTS ), root, true);
            final MountHandle mountHandle = new MountHandle( closable );
            
            ResourceRoot expandedResourceRoot = new ResourceRoot( root, mountHandle );
            unit.putAttachment( Attachments.DEPLOYMENT_ROOT, expandedResourceRoot );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
