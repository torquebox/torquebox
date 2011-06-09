package org.torquebox.web.websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.jboss.as.web.VirtualHost;
import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

public class WebSocketContext {

    WebSocketContext(WebSocketsServer server, VirtualHost host, String contextPath) {
        this.server = server;
        this.host = host;
        this.contextPath = contextPath;
    }
    
    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }
    
    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }
    
    public void setComponentResolver(ComponentResolver resolver) {
        this.resolver = resolver;
    }
    
    public ComponentResolver getComponentResolver() {
        return this.resolver;
    }
    
    public void setManager(Manager manager) {
        this.manager = manager;
    }
    
    public Manager getManager() {
        return this.manager;
    }
    
    public WebSocketProcessorComponent createComponent(Session session) throws Exception {
        Ruby runtime = this.runtimePool.borrowRuntime();
        WebSocketProcessorComponent component = (WebSocketProcessorComponent) this.resolver.resolve( runtime );
        component.setWebSocketContext(  this );
        component.setSession( session );
        return component;
    }
    
    public void releaseComponent(WebSocketProcessorComponent component) {
        Ruby runtime = component.getRubyComponent().getRuntime();
        this.runtimePool.returnRuntime( runtime );
    }
    
    public List<String> getHostNames() {
        List<String> hostNames = new ArrayList<String>(3);
        hostNames.add(  this.host.getHost().getName() );
        
        for ( String name : this.host.getHost().findAliases() ) {
            hostNames.add( name );
        }
        
        return hostNames;
    }
    
    public Session findSession(String sessionId) throws IOException {
        log.info( "manager: " + this.manager );
        Session session = manager.findSession( sessionId );
        log.info( "session: " + session );
        return session;
    }
    
    public String getContextPath() {
        return this.contextPath;
    }
    
    public void start() {
        this.server.registerContext( this );
    }
    
    public void stop() {
        this.server.unregisterContext( this );
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
    
    private WebSocketsServer server;
    
    private Manager manager;
    
    private VirtualHost host;
    private String contextPath;

    private RubyRuntimePool runtimePool;
    private ComponentResolver resolver;

}
