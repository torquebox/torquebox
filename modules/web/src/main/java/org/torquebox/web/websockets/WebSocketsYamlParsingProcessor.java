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
        
        return webSocketMetaData;
    }

}
