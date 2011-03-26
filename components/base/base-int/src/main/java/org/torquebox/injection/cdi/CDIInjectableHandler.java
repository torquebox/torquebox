package org.torquebox.injection.cdi;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.torquebox.injection.AbstractInjectableHandler;
import org.torquebox.injection.Injectable;


/** CDI injection handler.
 * 
 * Priority: 1,000
 * 
 * @author Bob McWhirter
 *
 */
public class CDIInjectableHandler extends AbstractInjectableHandler {

    public static final String TYPE = "cdi";

    public CDIInjectableHandler() {
        super( TYPE );
        setRecognitionPriority( 1 * 1000 );
    }

    @Override
    public Injectable handle(Node node, boolean generic) {
        String name = getJavaClassName( node );
        return new CDIInjectable( name, generic );
    }

    @Override
    public boolean recognizes(Node argsNode) {
        if (argsNode.getNodeType() == NodeType.ARRAYNODE) {
            ArrayNode argsArray = (ArrayNode) argsNode;
            if (argsArray.size() == 1) {
                Node root = argsArray.get( 0 );
                return (root.getNodeType() == NodeType.CALLNODE) || (root.getNodeType() == NodeType.VCALLNODE);
            }
        }

        return false;
    }
}