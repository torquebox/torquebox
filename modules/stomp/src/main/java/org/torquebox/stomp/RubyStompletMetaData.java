package org.torquebox.stomp;

import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

public class RubyStompletMetaData {

    public static final AttachmentKey<AttachmentList<RubyStompletMetaData>> ATTACHMENTS_KEY = AttachmentKey.createList( RubyStompletMetaData.class );
    
    public RubyStompletMetaData(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setDestinationPattern(String destinationPattern) {
        this.destinationPattern = destinationPattern;
    }
    
    public String getDestinationPattern() {
        return this.destinationPattern;
    }

    public void setRubyClassName(String rubyClassName) {
        this.rubyClassName = rubyClassName;
    }
    
    public String getRubyClassName() {
        return this.rubyClassName;
    }
    
    public void setRubyRequirePath(String rubyRequirePath) {
        this.rubyRequirePath = rubyRequirePath;
    }
    
    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }
    
    public void setStompletConfig(Map<String, String> stompletConfig) {
        this.stompletConfig = stompletConfig;
    }
    
    public Map<String, String> getStompletConfig() {
        return this.stompletConfig;
    }
        
    
    private String name;
    private String destinationPattern;
    private String rubyClassName;
    private String rubyRequirePath;
    private Map<String, String> stompletConfig;

}
