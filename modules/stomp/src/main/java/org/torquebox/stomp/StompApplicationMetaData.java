/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.stomp;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;

public class StompApplicationMetaData {
    
    public static final AttachmentKey<StompApplicationMetaData> ATTACHMENT_KEY = AttachmentKey.create( StompApplicationMetaData.class );
    public StompApplicationMetaData() {
        
    }
    
    public void addHost(String host) {
        this.hosts.add( host );
    }
    
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
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
    
    public String toString() {
        return "[StompApplicationMetaData: context=" + this.contextPath + "; hosts=" + this.hosts + "]";
    }
    
    private String contextPath;
    private List<String> hosts = new ArrayList<String>();



}
