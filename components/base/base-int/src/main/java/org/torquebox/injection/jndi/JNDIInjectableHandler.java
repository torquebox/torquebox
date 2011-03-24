package org.torquebox.injection.jndi;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

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