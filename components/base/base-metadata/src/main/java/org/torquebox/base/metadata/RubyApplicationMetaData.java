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

package org.torquebox.base.metadata;

import java.util.Map;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class RubyApplicationMetaData {
    
    public static final String DEFAULT_ENVIRONMENT_NAME = "development";

    private VirtualFile root;
    private String applicationName;
    private String environmentName;
    private Map<String, String> environment;
    private boolean archive = false;

    public RubyApplicationMetaData() {
    }
    
    public void applyDefaults() {
        if ( environmentName == null ) {
            this.environmentName = DEFAULT_ENVIRONMENT_NAME;
        }
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
    
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    public String getApplicationName() {
        return this.applicationName;
    }

    public boolean isArchive() {
        return this.archive;
    }

    public boolean isDevelopmentMode() {
        return environmentName == null || environmentName.trim().equalsIgnoreCase( "development" );
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getEnvironmentName() {
        return this.environmentName;
    }

    public void setEnvironmentVariables(Map<String, String> environment) {
        this.environment = environment;
    }

    public Map<String, String> getEnvironmentVariables() {
        return this.environment;
    }

    public String toString() {
        return "[RubyApplicationMetaData:\n  root=" + this.root + "\n  environmentName=" + this.environmentName + "\n  archive=" + this.archive + "\n  environment=" + this.environment + "]";
    }

}
