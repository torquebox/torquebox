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

package org.torquebox.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

public abstract class AbstractParsingProcessor implements DeploymentUnitProcessor {

    private static final String[] METADATA_LOCATIONS = new String[] { "", "config/" };

    public AbstractParsingProcessor() {
    }

    protected VirtualFile getMetaDataFile(final DeploymentUnit unit, final String fileName) {
        for (ResourceRoot each : allRoots( unit ) ) {
            VirtualFile root = each.getRoot();            
            for (int i = 0; i < METADATA_LOCATIONS.length; ++i) {
                final VirtualFile file = root.getChild( METADATA_LOCATIONS[i] + fileName );
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

    protected List<VirtualFile> getMetaDataFileBySuffix(final DeploymentUnit unit, final String suffix) {
        final List<VirtualFile> files = new ArrayList<VirtualFile>();
        for (ResourceRoot each : allRoots( unit ) ) {
            VirtualFile root = each.getRoot();            
            for (int i = 0; i < METADATA_LOCATIONS.length; ++i) {
                final VirtualFile file = root.getChild( METADATA_LOCATIONS[i] );
                try {
                    List<VirtualFile> matches = file.getChildren( new VirtualFileFilter() {
                            @Override
                            public boolean accepts(VirtualFile file) {
                                return file.getName().endsWith( suffix );
                            }
                        } );
                files.addAll( matches );
                } catch (IOException e) {
                    // TODO
                    // ignore?
                }
            }
        }

        return files;
    }
    
    protected List<ResourceRoot> allRoots(final DeploymentUnit unit) {
        final ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        final List<ResourceRoot> roots = unit.getAttachmentList( Attachments.RESOURCE_ROOTS );
        roots.add( resourceRoot );

        return roots;
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
    }

}
