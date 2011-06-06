package org.torquebox.services.injection;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/**
 * Handler for injecting Ruby services into other consumers.
 * 
 * @see ServiceInjectable
 * 
 * @author Bob McWhirter
 */
public class ServiceInjectableHandler extends AbstractInjectableHandler {

    public static final String TYPE = "service";

    public ServiceInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 6 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new ServiceInjectable( name.substring( 8 ) );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        return (str != null) && (str.startsWith( "service:" ));
    }

}