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

package org.torquebox.rack.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

public class RackApplicationMetaData {

    public RackApplicationMetaData() {

    }

    public void setRackUpScript(String rackUpScript) {
        this.rackUpScript = rackUpScript;
    }

    public String getRackUpScript(VirtualFile root) throws IOException {
        if (this.rackUpScript == null) {
            VirtualFile file = getRackUpScriptFile( root );
            if (file != null && file.exists()) {
                StringBuilder script = new StringBuilder();
                BufferedReader in = null;
                try {
                    in = new BufferedReader( new InputStreamReader( file.openStream() ) );
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        script.append( line );
                        script.append( "\n" );
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
                this.rackUpScript = script.toString();
            }
        }
        return this.rackUpScript;
    }

    public void setRackUpScriptLocation(String rackUpScriptLocation) {
        this.rackUpScriptLocation = rackUpScriptLocation;
    }

    public String getRackUpScriptLocation() {
        return this.rackUpScriptLocation;
    }

    public VirtualFile getRackUpScriptFile(VirtualFile root) {
        if (this.rackUpScriptLocation == null) {
            return null;
        }

        if (this.rackUpScriptLocation.startsWith( "/" ) || rackUpScriptLocation.matches( "^[A-Za-z]:.*" )) {
            return VFS.getChild( rackUpScriptLocation );
        } else {
            return root.getChild( rackUpScriptLocation );
        }
    }

    public void addHost(String host) {
        if (host != null && !this.hosts.contains( host ))
            this.hosts.add( host );
    }

    public List<String> getHosts() {
        return this.hosts;
    }

    public void setContextPath(String contextPath) {
        if (contextPath != null) this.contextPath = contextPath;
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

    public String toString() {
        return "[RackApplicationMetaData:" + System.identityHashCode( this ) + "\n  rackupScriptLocation=" + this.rackUpScriptLocation + "\n  rackUpScript="
                + this.rackUpScript + "\n  host=" + this.hosts + "\n  context=" + this.contextPath + "\n  static=" + this.staticPathPrefix + "]";
    }

    private String rackUpScript;
    private String rackUpScriptLocation = "config.ru";

    private List<String> hosts = new ArrayList<String>();
    private String contextPath = "/";
    private String staticPathPrefix = "public/";

    private String rubyRuntimePoolName;
    private String rackApplicationFactoryName;
    private String rackApplicationPoolName;
}
