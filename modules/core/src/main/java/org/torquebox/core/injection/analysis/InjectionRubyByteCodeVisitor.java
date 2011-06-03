package org.torquebox.core.injection.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jboss.msc.inject.InjectionException;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.types.INameNode;
import org.torquebox.core.analysis.AnalyzingVisitor;

public class InjectionRubyByteCodeVisitor extends AnalyzingVisitor {

    public InjectionRubyByteCodeVisitor(InjectionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Object visitFCallNode(FCallNode node) throws InjectionException {
        String callName = node.getName();

        if (!this.markerSeen && (callName.equals( "include" ) || callName.equals( "extend" ))) {
            String includedName = getConstString( ((ArrayNode) node.getArgsNode()).get( 0 ) );
            if (includedName.equals( TORQUEBOX_MARKER_MODULE )) {
                this.markerSeen = true;
            }
        } else {
            InjectableHandler handler = null;

            boolean generic = false;
            if (isValidInjectCall( node )) {
                Node argsNode = node.getArgsNode();
                handler = this.analyzer.getInjectableHandlerRegistry().getHandler( argsNode );

                if (handler == null) {
                    throw new AmbiguousInjectionException( node.getPosition(), getString( argsNode ) );
                }
                generic = true;
            } else if (callName.startsWith( INJECTION_PREFIX )) {
                String injectionType = callName.substring( INJECTION_PREFIX.length() );
                handler = this.analyzer.getInjectableHandlerRegistry().getHandler( injectionType );
                if (handler == null) {
                    throw new InvalidInjectionTypeException( node.getPosition(), injectionType );
                }
            }
            if (handler != null) {
                Injectable injectable = handler.handle( node.getArgsNode(), generic );
                if (injectable != null) {
                    this.injectables.add( injectable );
                }
            }
        }

        return null;
    }

    protected boolean isValidInjectCall(FCallNode node) {
    	String callName = node.getName();
    	
    	//check to make sure it's not a call to Enumerable#inject (it should have a block (iter) in that case)
    	return ("inject".equals( callName ) && null == node.getIterNode()) || 
    		"__inject__".equals( callName );
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

        while (!stack.isEmpty()) {
            str.append( stack.pop() );

            if (!stack.isEmpty()) {
                str.append( "::" );
            }
        }

        return str.toString();

    }

    public InjectionAnalyzer getAnalyzer() {
        return this.analyzer;
    }

    public Set<Injectable> getInjectables() {
        if (!this.markerSeen) {
            return Collections.emptySet();
        }
        return this.injectables;
    }
    
    public void reset() {
        this.markerSeen = false;
        this.injectables.clear();
    }

    private static final String INJECTION_PREFIX = "inject_";
    public static final String TORQUEBOX_MARKER_MODULE = "TorqueBox::Injectors";

    private InjectionAnalyzer analyzer;
    private boolean markerSeen = false;

    private Set<Injectable> injectables = new HashSet<Injectable>();

    @Override
    public Object getResult() {
        return getInjectables();
    }
}
