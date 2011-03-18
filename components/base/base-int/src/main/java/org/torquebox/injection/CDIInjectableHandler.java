package org.torquebox.injection;

import org.jruby.ast.Node;

public class CDIInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "cdi";

    public CDIInjectableHandler() {
        super( TYPE );
    }

    @Override
    public Injectable handle(Node node) {
        String name = getJavaClassName( node );
        return new CDIInjectable( name );
    }
    
}