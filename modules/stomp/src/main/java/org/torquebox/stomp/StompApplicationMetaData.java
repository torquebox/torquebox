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
