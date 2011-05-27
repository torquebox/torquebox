package org.torquebox.messaging.injection;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/** Handles MSC service injections.
 * 
 * Priority: 4,000
 * 
 * @author Bob McWhirter
 */
public class QueueInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "queue";

    public QueueInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 4 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new DestinationInjectable( "queue", name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        return (str != null ) && ( str.startsWith( "queue" ) || str.contains( "/queue" ) );
    }


    
}