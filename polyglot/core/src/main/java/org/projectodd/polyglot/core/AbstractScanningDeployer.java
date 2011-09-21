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

package org.projectodd.polyglot.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public abstract class AbstractScanningDeployer implements DeploymentUnitProcessor {

    private ArrayList<String> paths = new ArrayList<String>();
    private VirtualFileFilter filter;

    public AbstractScanningDeployer() {
    }

    public void addPath(String path) {
        this.paths.add(  path  );
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

    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        
        VirtualFile root = resourceRoot.getRoot();
        
        try {
            for (String path : this.paths) {
                VirtualFile scanRoot = root.getChild( path );

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
            throw new DeploymentUnitProcessingException( e );
        }
    }

    protected abstract void deploy(DeploymentUnit unit, VirtualFile file, String parentPath, String relativePath) throws DeploymentUnitProcessingException;
    
    @Override
    public void undeploy(DeploymentUnit context) {
    }

}
