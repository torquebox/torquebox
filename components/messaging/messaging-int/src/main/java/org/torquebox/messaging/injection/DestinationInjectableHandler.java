package org.torquebox.messaging.injection;

import org.jruby.ast.Node;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;

/** Queue and topic injection handler.
 * 
 * Priority: 5,000
 * 
 * @author Bob McWhirter
 *
 */
public class DestinationInjectableHandler extends AbstractInjectableHandler {

    public DestinationInjectableHandler(String type) {
        super( type );
        setRecognitionPriority( 5 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getString( node );
        return new DestinationInjectable( getType(), name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        String str = getString( argsNode );
        
        return ( str != null ) && ( str.contains( "/" + getType() ) );
    }
}