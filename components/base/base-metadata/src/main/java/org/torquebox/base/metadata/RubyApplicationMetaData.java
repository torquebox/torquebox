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

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

import java.util.Map;

public class RubyApplicationMetaData {

    private VirtualFile root;
    private String applicationName;
    private String environmentName;
    private boolean developmentMode = false;
    private Map<String, String> environment;
    private boolean archive = false;
    private String authenticationStrategy;
	private String authenticationDomain;

    public RubyApplicationMetaData() {
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

    public void setDevelopmentMode(boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

    public boolean isArchive() {
        return this.archive;
    }

    public boolean isDevelopmentMode() {
        return this.developmentMode;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getEnvironmentName() {
        return this.environmentName;
    }

    public String getAuthenticationStrategy() {
        return this.authenticationStrategy;
    }

    public void setAuthenticationStrategy(String authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

	public String getAuthenticationDomain() {
		return this.authenticationDomain;
	}
	
	public void setAuthenticationDomain(String domain) {
		this.authenticationDomain = domain;
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
