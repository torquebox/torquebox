package org.torquebox.injection.mc;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

public class MCBeanInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "mc";

    public MCBeanInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( Integer.MAX_VALUE );
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