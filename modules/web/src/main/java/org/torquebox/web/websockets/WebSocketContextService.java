package org.torquebox.web.websockets;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.jboss.as.web.VirtualHost;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class WebSocketContextService implements Service<WebSocketContext> {
    
    public WebSocketContextService(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public WebSocketContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info( "Starting websockets context: " + contextPath);
        log.info( "  server: " + this.serverInjector.getValue() );
        this.context = new WebSocketContext( this.serverInjector.getValue(), this.hostInjector.getValue(), contextPath );
        this.context.setRubyRuntimePool( this.runtimePoolInjector.getValue()  );
        this.context.setComponentResolver( this.resolverInjector.getValue() );
        Context webContext = this.contextInjector.getValue();
        this.context.setManager( webContext.getManager() );
        this.context.start();
    }

    @Override
    public void stop(StopContext context) {
        this.context.stop();
    }
    
    Injector<WebSocketsServer> getServerInjector() {
        return this.serverInjector;
    }
    
    Injector<VirtualHost> getHostInjector() {
        return this.hostInjector;
    }
    
    Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.runtimePoolInjector;
    }
    
    Injector<ComponentResolver> getComponentResolverInjector() {
        return this.resolverInjector;
    }
    
    Injector<Context> getContextInjector() {
        return this.contextInjector;
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );

    private InjectedValue<WebSocketsServer> serverInjector = new InjectedValue<WebSocketsServer>();
    private InjectedValue<VirtualHost> hostInjector = new InjectedValue<VirtualHost>();
    private InjectedValue<RubyRuntimePool> runtimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<ComponentResolver> resolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<Context> contextInjector = new InjectedValue<Context>();
    private String contextPath;
    
    private WebSocketContext context;
    
}
