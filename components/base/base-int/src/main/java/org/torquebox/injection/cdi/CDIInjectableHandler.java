package org.torquebox.injection.cdi;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

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