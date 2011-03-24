package org.torquebox.injection;

import java.util.HashMap;
import java.util.Map;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.jboss.logging.Logger;

public class InjectableHandlerRegistry {
    
    private static final Logger log = Logger.getLogger( InjectableHandlerRegistry.class );

    private Map<String, InjectableHandler> registry;

    public InjectableHandlerRegistry() {
        this.registry = new HashMap<String, InjectableHandler>();
    }
    
    public void start() {
        log.info( "Starting injectable handler registry");
        System.err.println( "BOB: Start registry" );
    }
    
    
    @Install
    public void addInjectableHandler(InjectableHandler handler) {
        System.err.println( "Registering injectable handler: " + handler.getType() + " - " + handler );
        log.info( "Registering injectable handler: " + handler.getType() + " - " + handler );
        this.registry.put( handler.getType(), handler );
    }
    
    @Uninstall
    public InjectableHandler removeInjectableHandler(InjectableHandler handler) {
        System.err.println( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        log.info( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        return this.registry.remove( handler.getType() );
    }

    public InjectableHandler getHandler(String type) {
        return type.startsWith("inject_") ? this.registry.get( type.substring(7) ) : null;
    }
}
