package org.torquebox.core.injection.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jruby.ast.Node;

public class InjectableHandlerRegistry implements Service<InjectableHandlerRegistry> {
    

    public InjectableHandlerRegistry() {
    }
    
    public void addInjectableHandler(InjectableHandler handler) {
        this.registry.put( handler.getType(), handler );
        this.handlersByPriority.add(  handler  );
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

    @Override
    public InjectableHandlerRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.injection.analysis" );

    private Map<String, InjectableHandler> registry = new HashMap<String, InjectableHandler>();
    private TreeSet<InjectableHandler> handlersByPriority = new TreeSet<InjectableHandler>( InjectableHandler.RECOGNITION_PRIORITY );
}
