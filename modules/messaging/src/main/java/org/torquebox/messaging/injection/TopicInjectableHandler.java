package org.torquebox.messaging.injection;

import org.jruby.ast.Node;
import org.torquebox.core.injection.analysis.AbstractInjectableHandler;
import org.torquebox.core.injection.analysis.Injectable;

/** Handles topic service injections.
 * 
 * Priority: 5,000
 * 
 * @author Bob McWhirter
 */
public class TopicInjectableHandler extends AbstractInjectableHandler {
    
    public static final String TYPE = "topic";

    public TopicInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 5 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new DestinationInjectable( "topic", name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        return ( str != null ) && ( str.startsWith( "topic" ) || str.contains( "/topic" ) );
    }


    
}