/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.core.analysis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InvisibleNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.jruby.ast.StrNode;
import org.jruby.ast.types.INameNode;
import org.jruby.ast.visitor.NodeVisitor;

public class DefaultNodeVisitor {

    private Logger log = Logger.getLogger( this.getClass() );

    private int indentLevel = 0;

    private boolean trace = false;

    private static final int MAX_INDENT_LEVEL = 100;

    protected Object defaultVisitNode(NodeVisitor proxy, Node node) {

        List<Object> results = new ArrayList<Object>();

        if (trace) {
            StringBuilder indent = new StringBuilder();

            if (!(node instanceof InvisibleNode)) {
                for (int i = 0; i < this.indentLevel; ++i) {
                    indent.append( "    " );
                }
                if (node instanceof INameNode) {
                    log.trace( indent + " " + node + " // " + getNodeName( node ) + ": " + ((INameNode) node).getName() );
                } else if (node instanceof StrNode) {
                    log.trace( indent + " " + node + " // " + getNodeName( node ) + ": " + ((StrNode) node).getValue() );
                } else {
                    log.trace( indent + " " + node + " // "  + getNodeName( node ) );
                }

            }
        }

        if (indentLevel > MAX_INDENT_LEVEL) {
            return null;
        }
        ++indentLevel;

        for (Node child : node.childNodes()) {
            if ( ! ( child.getNodeType() == NodeType.ARGUMENTNODE) && ! ( child.getNodeType() == NodeType.LISTNODE ) ) {
                try {
                    Object childResult = child.accept( proxy );
                    if (childResult != null) {
                        results.add( childResult );
                    }
                } catch(UnsupportedOperationException ex) {
                    log.trace( "JRuby doesn't support visiting node " + child + " - skipping it, but looking at its children." );
                    for (Node grandChild : child.childNodes()) {
                        defaultVisitNode( proxy, grandChild );
                    }
                }
            }
        }

        --indentLevel;

        if (results.isEmpty()) {
            return null;
        }

        if (results.size() == 1) {
            return results.get( 0 );
        }

        return results;
    }
    
    public Object visitFCallNode(NodeVisitor proxy, FCallNode iVisited) {
        return defaultVisitNode( proxy, iVisited );
    }

    protected String getNodeName(Node node) {
        String name = node.getClass().getName();
        int i = name.lastIndexOf( '.' );
        String nodeType = name.substring( i + 1 );
        return nodeType;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean isTrace() {
        return this.trace;
    }

    public static class DefaultNodeVisitorHandler implements InvocationHandler {
        private DefaultNodeVisitor visitor;

        public DefaultNodeVisitorHandler(DefaultNodeVisitor visitor) {
            this.visitor = visitor;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals( "visitFCallNode" ) &&
                    args.length == 1 && args[0] instanceof FCallNode) {
                return visitor.visitFCallNode( (NodeVisitor) proxy, (FCallNode) args[0] );
            }
            else if (method.getName().startsWith( "visit" ) && method.getName().endsWith( "Node" ) &&
                    args.length == 1 && args[0] instanceof Node) {
                return visitor.defaultVisitNode( (NodeVisitor) proxy, (Node) args[0] );
            } else {
                throw new RuntimeException( "Unexpected method '" + method.getName() + "' called" );
            }
        }
    }

}
