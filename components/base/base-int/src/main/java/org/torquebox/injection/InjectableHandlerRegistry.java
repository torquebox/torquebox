package org.torquebox.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.jboss.logging.Logger;
import org.jruby.ast.Node;

public class InjectableHandlerRegistry {
    
    private static final Logger log = Logger.getLogger( InjectableHandlerRegistry.class );

    private List<InjectableHandler> orderedHandlers;
    private Map<String, InjectableHandler> registry;

    public InjectableHandlerRegistry() {
        this.orderedHandlers = new ArrayList<InjectableHandler>();
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
        this.orderedHandlers.add( handler );
        this.registry.put( handler.getType(), handler );
    }
    
    @Uninstall
    public InjectableHandler removeInjectableHandler(InjectableHandler handler) {
        System.err.println( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        log.info( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        return this.registry.remove( handler.getType() );
    }

    public InjectableHandler getHandler(String type) {
        return this.registry.get( type );
    }

    public InjectableHandler getHandler(Node argsNode) {
        for ( InjectableHandler each : this.orderedHandlers ) {
            if ( each.recognizes( argsNode ) ) {
                return each;
            }
        }
        
        return null;
    }
}
