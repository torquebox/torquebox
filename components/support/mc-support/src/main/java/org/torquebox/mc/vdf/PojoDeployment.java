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

package org.torquebox.mc.vdf;

import java.io.IOException;

import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.util.automount.Automounter;
import org.jboss.vfs.util.automount.MountOption;

public class PojoDeployment {

    private static final Logger log = Logger.getLogger( PojoDeployment.class );

    private DeployerClient deployer;
    private VFSDeployment deployment;

    public PojoDeployment(DeployerClient deployer, VFSDeployment deployment) {
        this.deployer = deployer;
        this.deployment = deployment;
    }

    public VFSDeployment getDeployment() {
        return this.deployment;
    }

    public DeployerClient getMainDeployer() {
        return this.deployer;
    }

    public void start() throws DeploymentException {
        VirtualFile root = deployment.getRoot();

        if (!root.isDirectory()) {
            try {
                Automounter.mount( root, MountOption.COPY );
            } catch (IOException e) {
                throw new DeploymentException( e );
            }
        }

        this.deployer.addDeployment( this.deployment );
        log.info( "Deploying: " + this.deployment.getName() + " -- "+ this.deployment.getRoot() );
        this.deployer.process();
        this.deployer.checkComplete( this.deployment );
        log.info( "Fully deployed: " + this.deployment.getRoot() );
    }

    public void stop() throws DeploymentException {
        try {
            this.deployer.undeploy( this.deployment );
        } finally {
            VirtualFile root = deployment.getRoot();
            Automounter.cleanup( root );
        }

    }
}
