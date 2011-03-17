package org.torquebox.messaging.injection;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

public class DestinationInjectableHandler extends AbstractInjectableHandler {
    
    public DestinationInjectableHandler() {
    }

    @Override
    public Injectable handle(Node node) {
        String name = getString( node );
        return new DestinationInjectable( getType(), name );
    }
    
}