package org.torquebox.injection;

import org.jruby.ast.FCallNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.Node;
import org.torquebox.interp.analysis.DefaultNodeVisitor;

public class InjectionVisitor extends DefaultNodeVisitor {

    public InjectionVisitor(InjectionAnalyzer analyzer) {
        this.bodyVisitor = new InjectBlockVisitor( analyzer );
    }

    @Override
    public Object visitFCallNode(FCallNode node) {
        if (node.getName().equals( "torque" )) {
            Node genericBlockNode = node.getIterNode();
            if (genericBlockNode instanceof IterNode) {
                IterNode blockNode = (IterNode) genericBlockNode;

                Node bodyNode = blockNode.getBodyNode();
                return bodyNode.accept( this.bodyVisitor );
            }
            return null;
        } else {
            return super.visitFCallNode( node );
        }
    }

    private InjectBlockVisitor bodyVisitor;
}
