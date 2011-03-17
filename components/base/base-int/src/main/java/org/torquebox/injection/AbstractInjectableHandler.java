package org.torquebox.injection;

import org.jboss.logging.Logger;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.SymbolNode;

public abstract class AbstractInjectableHandler implements InjectableHandler {

    private Logger log = Logger.getLogger( this.getClass() );
    private String type;
    
    public AbstractInjectableHandler() {
    }
    
    public AbstractInjectableHandler(String type) {
        this.type = type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void start() {
        log.info(  "Starting" );
    }
    
    @Override
    public String getType() {
        return this.type;
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

}
