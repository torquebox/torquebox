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

package org.torquebox.base.deployers;

import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;

public abstract class AbstractParsingDeployer extends AbstractDeployer {

    public AbstractParsingDeployer() {
        setStage( DeploymentStages.PARSE );
    }

    protected VirtualFile getMetaDataFile(VFSDeploymentUnit unit, String fileName) {
        VirtualFile metaDataFile = unit.getAttachment( fileName + ".altDD", VirtualFile.class );

        if (metaDataFile == null) {
            metaDataFile = unit.getMetaDataFile( fileName );
        }

        return metaDataFile;
    }
    
    protected List<VirtualFile> getMetaDataFileBySuffix(VFSDeploymentUnit unit, String suffix) {
        return unit.getMetaDataFiles( null, suffix );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy( (VFSDeploymentUnit) unit );
        } else {
            throw new DeploymentException( "Deployer only accepts VFS deployment units." );
        }
    }

    protected abstract void deploy(VFSDeploymentUnit unit) throws DeploymentException;
}
