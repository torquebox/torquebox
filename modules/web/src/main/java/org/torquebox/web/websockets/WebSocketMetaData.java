package org.torquebox.web.websockets;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class WebSocketMetaData {
    
    public static final AttachmentKey<AttachmentList<WebSocketMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList( WebSocketMetaData.class );
    
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
    
    private String name;
    private String contextPath;
    private String rubyClassName;
    private String requirePath;
    

}
