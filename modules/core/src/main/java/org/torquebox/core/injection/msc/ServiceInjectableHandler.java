package org.torquebox.core.injection.msc;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/** Handles MSC service injections.
 * 
 * Priority: 100,000
 * 
 * @author Bob McWhirter
 */
public class ServiceInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "service";

    public ServiceInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 100 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new ServiceInjectable( name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        return true;
    }


    
}