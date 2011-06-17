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

import org.jboss.as.server.deployment.DeploymentUnit;
import org.torquebox.core.AbstractSplitYamlParsingProcessor;
import org.torquebox.core.util.StringUtils;

public class WebSocketsYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {
    public WebSocketsYamlParsingProcessor() {
        setSectionName( "websockets" );
    }

    @Override
    protected void parse(DeploymentUnit unit, Object baseData) throws Exception {
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) baseData;

        for (String name : data.keySet()) {
            WebSocketMetaData metaData = parse( name, data.get( name ) );
            unit.addToAttachmentList( WebSocketMetaData.ATTACHMENTS_KEY, metaData );
        }
    }

    protected WebSocketMetaData parse(String name, Map<String, Object> contextData) throws Exception {
        WebSocketMetaData webSocketMetaData = new WebSocketMetaData(name);
        
        String contextPath = (String) contextData.get( "context" );
        
        if ( contextPath == null || contextPath.trim().equals( "" ) ) {
            throw new Exception( "Websocket 'context' parameter is required for context: " + name );
        }
        
        webSocketMetaData.setContextPath( contextPath );
        
        String rubyClassName = (String) contextData.get( "class" );
        
        if ( rubyClassName == null || rubyClassName.trim().equals( "" ) ) {
            throw new Exception( "Websocket 'class' parameter is required for context: " + name );
        }
        
        webSocketMetaData.setRubyClassName( rubyClassName );
        
        String requirePath = StringUtils.underscore( rubyClassName );
        
        webSocketMetaData.setRequirePath( requirePath );
        
        Map<String,Object> config = (Map<String, Object>) contextData.get( "config" );
        webSocketMetaData.setRubyConfig( config );
        
        return webSocketMetaData;
    }

}
