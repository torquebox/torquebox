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

package org.torquebox.core.processors;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentMountProvider;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.MountType;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

/**
 * Handle mounting -knob.yml files and marking them as a DEPLOYMENT_ROOT
 * 
 */
public class KnobRootMountProcessor implements DeploymentUnitProcessor {
    
    public static final AttachmentKey<ResourceRoot> KNOB_ROOT = AttachmentKey.create(ResourceRoot.class);

    public KnobRootMountProcessor() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (deploymentUnit.getAttachment( Attachments.DEPLOYMENT_ROOT ) != null) {
            return;
        }

        if (!deploymentUnit.getName().endsWith( "-knob.yml" )) {
            return;
        }
        
        final DeploymentMountProvider deploymentMountProvider = deploymentUnit.getAttachment( Attachments.SERVER_DEPLOYMENT_REPOSITORY );
        if(deploymentMountProvider == null) {
            throw new DeploymentUnitProcessingException( "No deployment repository available." );
        }

        final VirtualFile deploymentContents = deploymentUnit.getAttachment( Attachments.DEPLOYMENT_CONTENTS );
        
        // internal deployments do not have any contents, so there is nothing to mount
        if (deploymentContents == null)
            return;

        String deploymentName = deploymentUnit.getName();
        final VirtualFile deploymentRoot = VFS.getChild( "content/" + deploymentName );
        Closeable handle = null;
        final MountHandle mountHandle;
        boolean failed = false;
        try {
            handle = deploymentMountProvider.mountDeploymentContent( deploymentContents, deploymentRoot, MountType.REAL );
            mountHandle = new MountHandle( handle );
        } catch (IOException e) {
            failed = true;
            throw new DeploymentUnitProcessingException( "Failed to mount -knob.yml file", e );
        } finally {
            if (failed) {
                VFSUtils.safeClose( handle );
            }
        }
        final ResourceRoot resourceRoot = new ResourceRoot( deploymentRoot, mountHandle );
        deploymentUnit.putAttachment( Attachments.DEPLOYMENT_ROOT, resourceRoot );
        deploymentUnit.putAttachment( KNOB_ROOT, resourceRoot );
        deploymentUnit.putAttachment( Attachments.MODULE_SPECIFICATION, new ModuleSpecification() );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        final ResourceRoot knobRoot = context.removeAttachment( KNOB_ROOT );
        if (knobRoot != null) {
            final MountHandle mountHandle = knobRoot.getMountHandle();
            VFSUtils.safeClose( mountHandle );
        }
    }

}
