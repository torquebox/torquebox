package org.torquebox.injection;

import java.util.Stack;

import org.jboss.logging.Logger;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.torquebox.core.injection.analysis.InjectableHandler;

public abstract class AbstractInjectableHandler implements InjectableHandler {

    private Logger log = Logger.getLogger( this.getClass() );
    private String type;
    private int recognitionPriority = 0;

    public AbstractInjectableHandler(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void start() {
        log.info( "Starting" );
    }

    @Override
    public String getType() {
        return this.type;
    }
    
    public int getRecognitionPriority() {
        return this.recognitionPriority;
    }
    
    public void setRecognitionPriority(int priority) {
        this.recognitionPriority = priority;
    }

    protected static String getString(Node node) {
        String str = null;

        if (node.getNodeType() == NodeType.STRNODE) {
            str = ((StrNode) node).getValue().toString();
        } else if (node.getNodeType() == NodeType.SYMBOLNODE) {
            str = ((SymbolNode) node).getName();
        } else if (node.getNodeType() == NodeType.ARRAYNODE) {
            if (((ArrayNode) node).size() == 1) {
                str = getString( ((ArrayNode) node).get( 0 ) );
            }
        }
        
        return str;
    }

    protected static String getJavaClassName(Node node) {
        Node cur = node;

        Stack<String> stack = new Stack<String>();

        while (cur != null) {
            if (cur.getNodeType() == NodeType.CALLNODE) {
                stack.push( ((CallNode) cur).getName() );
            } else if (cur.getNodeType() == NodeType.VCALLNODE) {
                stack.push( ((VCallNode) cur).getName() );
            }

            if (cur.childNodes().isEmpty()) {
                cur = null;
            } else {
                cur = cur.childNodes().get( 0 );
            }
        }

        StringBuilder name = new StringBuilder();

        while (!stack.isEmpty()) {
            name.append( stack.pop() );
            if (!stack.isEmpty()) {
                name.append( "." );
            }
        }

        return name.toString();
    }

}
