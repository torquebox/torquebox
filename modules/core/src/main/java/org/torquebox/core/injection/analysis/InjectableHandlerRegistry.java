package org.torquebox.core.injection.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
            System.err.println( "test: " + each );
            if ( each.recognizes( argsNode ) ) {
                System.err.println( "MATCH" );
                return each;
            }
        }
        
        return null;
    }
    
    public Set<Injectable> getPredeterminedInjectables() {
        Set<Injectable> injectables = new HashSet<Injectable>();
        for (InjectableHandler each : this.handlersByPriority ) {
            if ( each instanceof PredeterminedInjectableHandler ) {
                injectables.addAll( ((PredeterminedInjectableHandler)each).getInjectables());
            }
        }
        return injectables;
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
