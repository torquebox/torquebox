package org.torquebox.injection;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jboss.beans.metadata.api.annotations.Install;
import org.jboss.beans.metadata.api.annotations.Uninstall;
import org.jboss.logging.Logger;
import org.jruby.ast.Node;

public class InjectableHandlerRegistry {
    
    private static final Logger log = Logger.getLogger( InjectableHandlerRegistry.class );

    private Map<String, InjectableHandler> registry = new HashMap<String, InjectableHandler>();
    private TreeSet<InjectableHandler> handlersByPriority = new TreeSet<InjectableHandler>( InjectableHandler.RECOGNITION_PRIORITY );

    public InjectableHandlerRegistry() {
    }
    
    @Install
    public void addInjectableHandler(InjectableHandler handler) {
        log.info( "Registering injectable handler: " + handler.getType() + " - " + handler );
        System.err.println( "Registering injectable handler: " + handler.getType() + " - " + handler );
        this.registry.put( handler.getType(), handler );
        this.handlersByPriority.add(  handler  );
    }
    
    @Uninstall
    public InjectableHandler removeInjectableHandler(InjectableHandler handler) {
        log.info( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        System.err.println( "Unregistering injectable handler: " + handler.getType() + " - " + handler );
        this.handlersByPriority.remove( handler );
        return this.registry.remove( handler.getType() );
    }

    public InjectableHandler getHandler(String type) {
        return this.registry.get( type );
    }

    public InjectableHandler getHandler(Node argsNode) {
        
        for ( InjectableHandler each : this.handlersByPriority ) {
            if ( each.recognizes( argsNode ) ) {
                return each;
            }
        }
        
        return null;
    }
    
}
