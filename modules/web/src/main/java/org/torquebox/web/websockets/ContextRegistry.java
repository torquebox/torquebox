package org.torquebox.web.websockets;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

public class ContextRegistry {

    public ContextRegistry() {

    }

    public void addContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for (String hostName : context.getHostNames()) {
            String key = makeKey( hostName, contextPath );
            log.info( "Link context: '" + key + "' to " + context );
            this.registry.put( key, context );
        }
    }

    public void removeContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for (String hostName : context.getHostNames()) {
            this.registry.remove( makeKey( hostName, contextPath ) );
        }
    }

    public WebSocketContext findContext(String host, String uri) {
        log.info(  "Find context: " + makeKey( host, uri )  );
        return this.registry.get( makeKey( host, uri ) );
    }
    
    protected static String makeKey(String host, String uri) {
        String hostWithoutPort = host;
        int colonLoc = host.indexOf(  ":"  );
        if ( colonLoc >= 0 ) {
            hostWithoutPort = host.substring( 0, colonLoc );
        } 
        
        return hostWithoutPort + ":" + uri;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
    private Map<String, WebSocketContext> registry = new HashMap<String, WebSocketContext>();

}
