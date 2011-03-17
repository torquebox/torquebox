package org.torquebox.injection;

import org.jruby.ast.Node;

public class JNDIInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "jndi";

    public JNDIInjectableHandler() {
        super( TYPE );
    }

    @Override
    public Injectable handle(Node node) {
        String name = getString( node );
        return new JNDIInjectable( name );
    }
    
}