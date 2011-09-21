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

package org.projectodd.polyglot.web;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

public class WebApplicationMetaData {
    public static final AttachmentKey<WebApplicationMetaData> ATTACHMENT_KEY = AttachmentKey.create( WebApplicationMetaData.class );
    
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
    
    public void attach(DeploymentUnit unit) {
        unit.putAttachment( ATTACHMENT_KEY, this );
    }

    private List<String> hosts = new ArrayList<String>();
    private String contextPath = "/";
    private String staticPathPrefix = "public/";
}
