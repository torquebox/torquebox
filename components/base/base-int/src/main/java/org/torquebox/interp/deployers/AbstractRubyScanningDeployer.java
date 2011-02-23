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

package org.torquebox.interp.deployers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public abstract class AbstractRubyScanningDeployer extends AbstractDeployer {

    private ArrayList<String> paths;
    private VirtualFileFilter filter;

    public AbstractRubyScanningDeployer() {
        setStage( DeploymentStages.PARSE );
    }

    public void setPaths(ArrayList<String> paths) {
        this.paths = paths;
    }

    public ArrayList<String> getPaths() {
        return this.paths;
    }

    public void setFilter(VirtualFileFilter filter) {
        this.filter = filter;
    }

    public void setSuffixFilter(String suffix) {
        this.filter = new SuffixMatchFilter( suffix, VisitorAttributes.DEFAULT );
    }

    public VirtualFileFilter getFilter() {
        return this.filter;
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (!(unit instanceof VFSDeploymentUnit)) {
            throw new DeploymentException( "Deployment unit must be a VFSDeploymentUnit" );
        }

        deploy( (VFSDeploymentUnit) unit );
    }

    protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        try {
            for (String path : this.paths) {
                VirtualFile scanRoot = unit.getRoot().getChild( path );

                if (scanRoot == null || !scanRoot.exists()) {
                    continue;
                }

                List<VirtualFile> children = null;

                if (this.filter != null) {
                    children = scanRoot.getChildrenRecursively( this.filter );
                } else {
                    children = scanRoot.getChildrenRecursively();
                }

                int prefixLength = scanRoot.getPathName().length();

                for (VirtualFile child : children) {
                    String relativePath = child.getPathName().substring( prefixLength );
                    deploy( unit, child, path, relativePath.substring( 1 ) );
                }
            }
        } catch (IOException e) {
            throw new DeploymentException( e );
        }
    }

    protected abstract void deploy(VFSDeploymentUnit unit, VirtualFile file, String parentPath, String relativePath) throws DeploymentException;

}
