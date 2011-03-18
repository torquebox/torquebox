package org.torquebox.injection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.types.INameNode;
import org.torquebox.interp.analysis.DefaultNodeVisitor;

public class InjectionVisitor extends DefaultNodeVisitor {

    public InjectionVisitor(InjectionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Object visitFCallNode(FCallNode node) {
        String callName = node.getName();

        if ( ! this.markerSeen && callName.equals( "include" )) {
            String includedName = getConstString( ((ArrayNode) node.getArgsNode()).get( 0 ) );
            if ( includedName.equals( TORQUEBOX_MARKER_MODULE ) ) {
                this.markerSeen = true;
            }
        } else {
            InjectableHandler handler = this.analyzer.getInjectableHandlerRegistry().getHandler( callName );
            if (handler != null) {
                Injectable injectable = handler.handle( node.getArgsNode() );
                if ( injectable != null ) {
                    this.injectables.add(  injectable  );
                }
            }
        }

        return null;
    }

    protected static String getString(Node node) {
        String str = null;

        if (node.getNodeType() == NodeType.STRNODE) {
            str = ((StrNode) node).getValue().toString();
        } else if (node instanceof INameNode) {
            str = ((INameNode) node).getName();
        } else if (node.getNodeType() == NodeType.ARRAYNODE) {
            if (((ArrayNode) node).size() == 1) {
                str = getString( ((ArrayNode) node).get( 0 ) );
            }
        }
        return str;
    }

    protected static String getConstString(Node node) {
        Stack<String> stack = new Stack<String>();

        Node cur = node;

        while (cur != null) {

            stack.push( ((INameNode) cur).getName() );

            List<Node> children = cur.childNodes();
            if (children == null || children.isEmpty()) {
                break;
            }

            cur = children.get( 0 );
        }
        
        StringBuilder str = new StringBuilder();
        
        while ( ! stack.isEmpty() ) {
            str.append( stack.pop() );
            
            if ( ! stack.isEmpty() ) {
                str.append( "::" );
            }
        }
        
        return str.toString();

    }
    
    public List<Injectable> getInjectables() {
        if ( ! this.markerSeen ) {
            return Collections.emptyList();
        }
        return this.injectables;
    }

    public static final String TORQUEBOX_MARKER_MODULE = "TorqueBox::Injectors";
    
    private InjectionAnalyzer analyzer;
    private boolean markerSeen = false;
    
    private List<Injectable> injectables = new ArrayList<Injectable>();
}
