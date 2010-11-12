/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.rack.metadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.interp.spi.RuntimeInitializer;


public class RackApplicationMetaData {
	
	private String rackEnv;
	private VirtualFile rackRoot;
	private String rackUpScript;
	private VirtualFile rackUpScriptLocation;
	
	private List<String> hosts = new ArrayList<String>();
	private String contextPath;
	private String staticPathPrefix;
	
	private String rubyRuntimePoolName;
	private String rackApplicationFactoryName;
	private String rackApplicationPoolName;
	private Map<String, String> environment;
    private RuntimeInitializer runtimeInitializer;

	public RackApplicationMetaData() {
		
	}
	
	public void setRackRoot(VirtualFile rackRoot) {
		this.rackRoot = rackRoot;
	}

    public void setRackRoot(String path) {
        if (path != null) setRackRoot( VFS.getChild( path ) );
    }

	public VirtualFile getRackRoot() {
		return this.rackRoot;
	}
	
	public void setRackEnv(String rackEnv) {
		this.rackEnv = rackEnv;
	}
	
	public String getRackEnv() {
		return this.rackEnv;
	}
	
	public void setRackUpScript(String rackUpScript) {
		this.rackUpScript = rackUpScript;
	}

	public String getRackUpScript() {
		return this.rackUpScript;
	}
	
	public void setRackUpScriptLocation(VirtualFile rackUpScriptLocation) {
		this.rackUpScriptLocation = rackUpScriptLocation;
	}
	
	public VirtualFile getRackUpScriptLocation() {
		return this.rackUpScriptLocation;
	}
	
    public void setRackUpScript(VirtualFile file) throws IOException {
        if (file != null && file.exists()) {
            StringBuilder script = new StringBuilder();
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(file.openStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    script.append(line);
                    script.append("\n");
                }
            } finally {
                if (in != null) in.close();
            }
            setRackUpScript( script.toString() );
        }
        setRackUpScriptLocation( file );
    }

    public void setRackUpScriptPath(String path) throws IOException {
        if (path != null) {
            setRackUpScript( (path.startsWith("/") || path.matches("^[A-Za-z]:.*")) ? VFS.getChild(path) : getRackRoot().getChild(path) );
        }
    }

	public void addHost(String host) {
		if ( ! this.hosts.contains(host) ) this.hosts.add( host );
	}
	
	public List<String> getHosts() {
		return this.hosts;
	}
	
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	public String getContextPath() {
		return this.contextPath;
	}
	
	public void setStaticPathPrefix(String staticPathPrefix) {
		this.staticPathPrefix = staticPathPrefix;
	}
	
	public String getStaticPathPrefix() {
		return this.staticPathPrefix;
	}
	
	public void setRubyRuntimePoolName(String rubyRuntimePoolName) {
		this.rubyRuntimePoolName = rubyRuntimePoolName;
	}
	
	public String getRubyRuntimePoolName() {
		return this.rubyRuntimePoolName;
	}
	
	public void setRackApplicationFactoryName(String rackApplicationFactoryName) {
		this.rackApplicationFactoryName = rackApplicationFactoryName;
	}
	
	public String getRackApplicationFactoryName() {
		return this.rackApplicationFactoryName;
	}

	public void setRackApplicationPoolName(String rackApplicationPoolName) {
		this.rackApplicationPoolName = rackApplicationPoolName;
	}
	
	public String getRackApplicationPoolName() {
		return this.rackApplicationPoolName;
	}

	public void setEnvironmentVariables(Map<String,String> environment) {
		this.environment = new HashMap<String,String>(environment);
	}

	public Map<String, String> getEnvironmentVariables() {
		return this.environment;
	}

    public void setRuntimeInitializer(RuntimeInitializer initializer) {
        this.runtimeInitializer = initializer;
    }

    public RuntimeInitializer getRuntimeInitializer() {
        return this.runtimeInitializer;
    }

	public String toString() {
		return "\n[RackApplicationMetaData:\n  rackEnv=" + this.rackEnv + "\n  rackRoot=" + this.rackRoot + "\n  rackUpScript=" + this.rackUpScript + "\n  rackUpScriptLocation=" + this.rackUpScriptLocation + "\n  contextPath=" + this.contextPath + "\n  staticPathPrefix=" + this.staticPathPrefix + "\n  runtimePool=" + this.rubyRuntimePoolName + "\n  appFactory=" + this.rackApplicationFactoryName + "\n  appPool=" + this.rackApplicationPoolName + "]";
	}
	
}
