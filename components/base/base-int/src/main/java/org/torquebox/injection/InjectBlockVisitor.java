package org.torquebox.injection;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.torquebox.interp.analysis.DefaultNodeVisitor;

public class InjectBlockVisitor extends DefaultNodeVisitor {
    
    public InjectBlockVisitor(InjectionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Object visitFCallNode(FCallNode node) {
        if (node.getName().equals( "inject" )) {
            Node genericArgsNode = node.getArgsNode();

            if (genericArgsNode instanceof ArrayNode) {
                ArrayNode argsNode = (ArrayNode) genericArgsNode;

                if (argsNode.size() == 2) {
                    Node siteNode = argsNode.get( 0 );
                    Node injectableNode = argsNode.get( 1 );

                    Injectable injectable = (Injectable) injectableNode.accept(  this  );
                    return injectable;
                }
            }
        } else {
            String type = node.getName();
            InjectableHandler handler = this.analyzer.getInjectableHandlerRegistry().getHandler( type );
            if ( handler != null ) {
                return handler.handle(  node.getArgsNode() );
            }
        }

        return null;
    }

    protected static String getString(Node node) {
        String str = null;

        if (node.getNodeType() == NodeType.STRNODE) {
            str = ((StrNode) node).getValue().toString();
        } else if (node.getNodeType() == NodeType.SYMBOLNODE) {
            str = ((SymbolNode) node).getName();
        } else if ( node.getNodeType() == NodeType.ARRAYNODE ) {
            if ( ((ArrayNode)node) .size() == 1 ) {
                str = getString( ((ArrayNode)node).get( 0 ) );
            }
        }
        return str;
    }

    private InjectionAnalyzer analyzer;


}
