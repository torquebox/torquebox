package org.torquebox.web.websockets;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.server.deployment.AttachmentKey;

public class URLRegistry {
    
    public static final AttachmentKey<URLRegistry> ATTACHMENT_KEY = AttachmentKey.create( URLRegistry.class );
    
    public URLRegistry() {
    }
    
    public String lookupURL(String socketName) {
        System.err.println( "lookup: " + socketName );
        return this.urls.get( socketName );
    }
    
    public void registerURL(String socketName, String url) {
        System.err.println( "registering: "+ socketName );
        System.err.println( "  url: " + url );
        this.urls.put( socketName, url );
    }

    private final Map<String,String> urls = new HashMap<String,String>();
}
