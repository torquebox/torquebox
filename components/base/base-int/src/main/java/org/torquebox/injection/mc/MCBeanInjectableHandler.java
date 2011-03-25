package org.torquebox.injection.mc;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

public class MCBeanInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "mc";

    public MCBeanInjectableHandler() {
        super( TYPE );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new MCBeanInjectable( name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        
        return ( str != null ) && str.matches( "^[^:]+:[^=]+=.*$" );
    }
    
}