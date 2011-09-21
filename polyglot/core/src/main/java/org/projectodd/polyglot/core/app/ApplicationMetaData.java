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

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public abstract class ApplicationMetaData {
    public static final AttachmentKey<ApplicationMetaData> ATTACHMENT_KEY = AttachmentKey.create( ApplicationMetaData.class );
    
    public ApplicationMetaData(String applicationName) {
        this.applicationName = sanitize( applicationName );
    }

    public void setRoot(VirtualFile root) {
        this.root = root;
    }

    public void setRoot(String path) {
        if (path != null) {
            String sanitizedPath = null;

            if (path.indexOf( "\\\\" ) >= 0) {
                sanitizedPath = path.replaceAll( "\\\\\\\\", "/" );
                sanitizedPath = sanitizedPath.replaceAll( "\\\\", "" );
            } else {
                sanitizedPath = path.replaceAll( "\\\\", "/" );
            }
            VirtualFile root = VFS.getChild( sanitizedPath );
            setRoot( root );
        }
    }

    public VirtualFile getRoot() {
        return this.root;
    }

    public String getRootPath() {
        try {
            return getRoot().toURL().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void explode(VirtualFile root) {
        this.root = root;
        this.archive = true;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public boolean isArchive() {
        return this.archive;
    }

    
    public void attach(DeploymentUnit unit) {
        unit.putAttachment( ATTACHMENT_KEY, this );
    }
    
    protected String sanitize(String name) {
        int lastSlash = name.lastIndexOf( "/" );
        if ( lastSlash >= 0 ) {
            name = name.substring( lastSlash+1 );
        }
        int lastDot = name.lastIndexOf( "." );
        if (lastDot >= 0) {
            name = name.substring( 0, lastDot );
        }
        int lastKnob = name.lastIndexOf( "-knob" );
        if (lastKnob >= 0) {
            name = name.substring( 0, lastKnob );
        }
        return name.replaceAll( "\\.", "-" );
    }
    
    private VirtualFile root;
    private String applicationName;
    private boolean archive = false;
}
