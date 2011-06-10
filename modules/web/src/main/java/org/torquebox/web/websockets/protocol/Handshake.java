package org.torquebox.web.websockets.protocol;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.torquebox.web.websockets.WebSocketContext;

public abstract class Handshake {
    
    public Handshake(String version) {
        this.version = version;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    protected String getWebSocketLocation(WebSocketContext context, HttpRequest request) {
        return "ws://" + request.getHeader( HttpHeaders.Names.HOST ) + context.getContextPath();
    }
    
    public abstract boolean matches(HttpRequest request);
    public abstract HttpResponse generateResponse(WebSocketContext context, HttpRequest request) throws Exception;
    
    

    private String version;

}
