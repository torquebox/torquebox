package org.torquebox.messaging.injection;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

public class DestinationInjectableHandler extends AbstractInjectableHandler {
    
    public DestinationInjectableHandler() {
    }
    
    public DestinationInjectableHandler(String type) {
        super( type );
    }


    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new DestinationInjectable( getType(), name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        
        return ( str != null ) && ( str.startsWith(  "/queues" ) || str.startsWith( "/topics" ) );
    }
    
}