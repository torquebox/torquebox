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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.app.ApplicationMetaData;

public class ArchiveDirectoryMountingProcessor implements DeploymentUnitProcessor {

    private final AttachmentKey<AttachmentList<Closeable>> CLOSEABLE_ATTACHMENTS_KEY = AttachmentKey.createList( Closeable.class );

    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ApplicationMetaData appMetaData = unit.getAttachment( ApplicationMetaData.ATTACHMENT_KEY );

        if (appMetaData == null) {
            return;
        }

        if (appMetaData.isArchive()) {
            try {
                mountDir( unit, appMetaData.getRoot(), "log", System.getProperty( "jboss.server.log.dir" ) + "/" + appMetaData.getApplicationName() );
                mountDir( unit, appMetaData.getRoot(), "tmp", System.getProperty( "jboss.server.temp.dir" ) + "/app/" + appMetaData.getApplicationName() );
            } catch (Exception e) {
                throw new DeploymentUnitProcessingException( e );
            }
        }
    }

    public void undeploy(DeploymentUnit unit) {

        List<Closeable> mounts = unit.getAttachmentList( CLOSEABLE_ATTACHMENTS_KEY );

        for (Closeable eachMount : mounts) {
            try {
                eachMount.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    protected void mountDir(DeploymentUnit unit, VirtualFile root, String name, String path) throws IOException {
        VirtualFile logical = root.getChild( name );
        File physical = new File( path );
        physical.mkdirs();
        Closeable mount = VFS.mountReal( physical, logical );
        unit.addToAttachmentList( CLOSEABLE_ATTACHMENTS_KEY, mount );
    }
}
