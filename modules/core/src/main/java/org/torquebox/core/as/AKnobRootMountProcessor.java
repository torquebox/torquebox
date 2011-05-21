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

import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * Replace the ServerDeploymentRepository with our own that understands
 * -knob.yml files for -knob.yml deployments.
 * 
 * Note: We need the "A" prefix to sort before DeploymentRootMountProcessor
 * because both have a priority of 0 in Phase.STRUCTURE
 * 
 */
public class AKnobRootMountProcessor implements DeploymentUnitProcessor {

    public AKnobRootMountProcessor(ServerEnvironment environment) {
        this.environment = environment;
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

        VirtualFile root = deploymentUnit.getAttachment( Attachments.DEPLOYMENT_CONTENTS );

        String deploymentName = deploymentUnit.getName();
        //System.err.println( "NAME [" + deploymentName + "]" );
        VirtualFile realRoot = root.getChild( deploymentName );
        try {
            Closeable closeable = VFS.mountReal( root.getPhysicalFile(), realRoot );
            MountHandle handle = new MountHandle( closeable );
            ResourceRoot expandedResourceRoot = new ResourceRoot( realRoot, handle );
            deploymentUnit.putAttachment( Attachments.DEPLOYMENT_ROOT, expandedResourceRoot );
            deploymentUnit.putAttachment( Attachments.MODULE_SPECIFICATION, new ModuleSpecification() );
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private ServerEnvironment environment;

}
