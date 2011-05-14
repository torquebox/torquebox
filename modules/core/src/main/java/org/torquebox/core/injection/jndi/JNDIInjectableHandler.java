package org.torquebox.core.injection.jndi;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/** Handles naming injections.
 * 
 * Priority: 10,000
 * 
 * @author Bob McWhirter
 */
public class JNDIInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "jndi";

    public JNDIInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 10 * 1000 );
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