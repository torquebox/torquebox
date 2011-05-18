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
import java.io.File;
import java.io.IOException;

import org.jboss.as.server.deployment.api.ServerDeploymentRepository;
import org.jboss.as.server.deployment.impl.ServerDeploymentRepositoryImpl;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.TorqueBox;

/**
 * Custom ServerDeploymentRepository implementation that handles mounting
 * -knob.yml files and delegates to an existing ServerDeploymentRepositoryImpl
 * for everything else
 *
 */
public class KnobServerDeploymentRepositoryImpl extends ServerDeploymentRepositoryImpl {
    
    public KnobServerDeploymentRepositoryImpl(final ServerDeploymentRepository originalRepository, final File repoRoot, final File systemDeployDir) {
        super( repoRoot, systemDeployDir );
        this.originalRepository = originalRepository;
    }

    @Override
    public Closeable mountDeploymentContent(String name, String runtimeName, byte[] deploymentHash, VirtualFile mountPoint, boolean mountExpanded) throws IOException {
        if (name.matches( TorqueBox.EXTERNAL_DESCRIPTOR_REGEX )) {
            final File content = getDeploymentContentFile(deploymentHash);
            return VFS.mountReal( content, mountPoint );
        } else {
            return this.originalRepository.mountDeploymentContent(name, runtimeName, deploymentHash, mountPoint, mountExpanded);
        }
    }



    private ServerDeploymentRepository originalRepository;
}
