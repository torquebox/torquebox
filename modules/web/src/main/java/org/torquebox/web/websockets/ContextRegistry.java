package org.torquebox.web.websockets;

import java.util.HashMap;
import java.util.Map;

public class ContextRegistry {
    

    public ContextRegistry() {
        
    }
    
    public void addContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for ( String hostName : context.getHostNames() ) {
            this.registry.put( hostName + contextPath, context );
        }
    }
    
    public void removeContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for ( String hostName : context.getHostNames() ) {
            this.registry.remove( hostName + contextPath );
        }
    }
    
    public WebSocketContext findContext(String host, String uri) {
        return this.registry.get(  host + uri  );
    }
    
    private Map<String, WebSocketContext> registry = new HashMap<String, WebSocketContext>();

}
