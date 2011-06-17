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

package org.torquebox.web.websockets;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

/**
 * Configuration for a web-socket endpoint.
 * 
 * <p>
 * Each web-socket endpoint is named uniquely within the scope of an
 * application, has a context-path scoped underneath the application's own web
 * context-path, and is serviced by implementations of a specific Ruby class.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class WebSocketMetaData {

    public static final AttachmentKey<AttachmentList<WebSocketMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList( WebSocketMetaData.class );

    /** Construct with a name.
     * 
     * @param name The name.
     */
    public WebSocketMetaData(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public void setRequirePath(String requirePath) {
        this.requirePath = requirePath;
    }

    public String getRequirePath() {
        return this.requirePath;
    }
    
    public void setRubyConfig(Map<String,Object> rubyConfig) {
        this.rubyConfig = rubyConfig;
    }
    
    public Map<String,Object> getRubyConfig() {
        return this.rubyConfig;
    }

    private String name;
    private String contextPath;
    private String rubyClassName;
    private String requirePath;
    private Map<String,Object> rubyConfig;

}
