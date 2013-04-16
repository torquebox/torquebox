/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.projectodd.polyglot.web.WebApplicationMetaData;

public class RackMetaData extends WebApplicationMetaData {

    public static final AttachmentKey<RackMetaData> ATTACHMENT_KEY = AttachmentKey.create(RackMetaData.class);
    
    public RackMetaData() {

    }

    @Override
    public void attachTo(DeploymentUnit unit) {
        super.attachTo( unit );
        unit.putAttachment( ATTACHMENT_KEY, this );
    }
    
    public void setRackUpScript(String rackUpScript) {
        this.rackUpScript = rackUpScript;
    }

    public String getRackUpScript(File root) throws IOException {
        return this.rackUpScript;
    }

    public void setRackUpScriptLocation(String rackUpScriptLocation) {
        this.rackUpScriptLocation = rackUpScriptLocation;
    }

    public String getRackUpScriptLocation() {
        return this.rackUpScriptLocation;
    }

    public File getRackUpScriptFile(File root) {
        if (this.rackUpScriptLocation == null) {
            return null;
        }

        if (this.rackUpScriptLocation.startsWith( "/" ) || rackUpScriptLocation.matches( "^[A-Za-z]:.*" )) {
            return new File( rackUpScriptLocation );
        } else {
            return new File( root, rackUpScriptLocation );
        }
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
                + this.rackUpScript + "\n  host=" + getHosts() + "\n  context=" + getContextPath() + "\n  static=" + getStaticPathPrefix() + "]";
    }

    private String rackUpScript;
    private String rackUpScriptLocation = "config.ru";

    private String rubyRuntimePoolName;
    private String rackApplicationFactoryName;
    private String rackApplicationPoolName;
}
