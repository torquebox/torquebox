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
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new JNDIInjectable( name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        
        return (str != null) && str.startsWith(  "java:" );
        
    }
    
}