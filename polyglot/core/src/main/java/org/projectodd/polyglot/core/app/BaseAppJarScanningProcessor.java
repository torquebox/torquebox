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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleRootMarker;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.server.deployment.module.TempFileProviderService;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;
import org.projectodd.polyglot.core.as.KnobDeploymentMarker;

public class BaseAppJarScanningProcessor implements DeploymentUnitProcessor {

    public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter( ".jar", VisitorAttributes.DEFAULT );
    
    public BaseAppJarScanningProcessor(List<String> scanRoots) {
        this.scanRoots = scanRoots;
    }
    
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        ApplicationMetaData appMetaData = unit.getAttachment( ApplicationMetaData.ATTACHMENT_KEY );
        
        if (appMetaData == null && !KnobDeploymentMarker.isMarked( unit )) {
            return;
        }
        
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();

        try {

            for (String scanRoot : this.scanRoots) {
                for (VirtualFile child : getJarFiles( root.getChild( scanRoot ) )) {
                    final Closeable closable = child.isFile() ? mount( child, false ) : null;
                    final MountHandle mountHandle = new MountHandle( closable );
                    final ResourceRoot childResource = new ResourceRoot( child, mountHandle );
                    ModuleRootMarker.mark(childResource);
                    unit.addToAttachmentList( Attachments.RESOURCE_ROOTS, childResource );
                }
            }

        } catch (IOException e) {
            log.error( "Error processing jars", e );
        }

    }
    
    public List<VirtualFile> getJarFiles(VirtualFile dir) throws IOException {
        return dir.getChildrenRecursively( JAR_FILTER );
    }
    
    private static Closeable mount(VirtualFile moduleFile, boolean explode) throws IOException {
        return explode ? VFS.mountZipExpanded( moduleFile, moduleFile, TempFileProviderService.provider() )
                       : VFS.mountZip( moduleFile, moduleFile, TempFileProviderService.provider() );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
    
    private List<String> scanRoots;
    
    private static final Logger log = Logger.getLogger( "org.torquebox.core.app" );

}
