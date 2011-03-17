package org.torquebox.injection;

import org.jruby.ast.Node;

public class MCBeanInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "mc";

    public MCBeanInjectableHandler() {
        super( TYPE );
    }

    @Override
    public Injectable handle(Node node) {
        String name = getString( node );
        return new MCBeanInjectable( name );
    }
    
}