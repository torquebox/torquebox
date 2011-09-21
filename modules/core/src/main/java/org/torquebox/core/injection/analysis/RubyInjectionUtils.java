/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.injection.analysis;

import static org.jruby.ast.NodeType.CALLNODE;
import static org.jruby.ast.NodeType.VCALLNODE;

import java.util.List;
import java.util.Stack;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.StrNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;

public class RubyInjectionUtils {

    /**
     * Convenience method to try real hard to convert an AST node into a String.
     * 
     * @param node
     *            The node.
     * @return The string, if possible, otherwise <code>null</code>.
     */

    protected static String getString(Node node) {
        String str = null;

        if (node instanceof INameNode) {
            str = ((INameNode) node).getName();
        } else {
            switch( node.getNodeType() ) {
                
            case ARRAYNODE:
                str = getString( (ArrayNode) node);
                break;
                
            case STRNODE:
                str = ((StrNode) node).getValue().toString();
                break;
                
            }
        }

        return str;
    }

    protected static String getString(ArrayNode node) {
        StringBuffer str = new StringBuffer();
        String nodeStr = getString( ((ArrayNode) node).get( 0 ) );
        str.append( nodeStr == null ? "<unknown>" : nodeStr );
        for(int i = 1; i < ((ArrayNode) node).size(); i++) {
            str.append( ", " );
            nodeStr = getString( ((ArrayNode) node).get( i ) );
            str.append( nodeStr == null ? "<unknown>" : nodeStr );
        }
        
        return str.toString();
    }


    /**
     * Attempt to treat the argument node as a tree describing a ruby constant,
     * and extract it into a String.
     * 
     * @param node The constant AST root.
     * @return The string flavor of the constant.
     */
    protected static String getConstString(Node node) {
        Stack<String> stack = new Stack<String>();

        Node cur = node;

        while (cur != null) {
            
            if ( ! ( cur instanceof INameNode) ) {
                return "";
            }

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

    protected static boolean isLegalInjection(Node node) {
        switch( node.getNodeType() ) {

        case CALLNODE:
        case STRNODE:
        case SYMBOLNODE:
        case VCALLNODE:
            return true;

        case ARRAYNODE:
            if (((ArrayNode) node).size() == 1) {
                return isLegalInjection( ((ArrayNode) node).get( 0 ) );
            } else {
                throw new IllegalInjectionException( node.getPosition(), 
                                                     getString( node ), 
                                                     "inject takes only one argument" );
            }

        case INSTVARNODE:
            throw new IllegalInjectionException( node.getPosition(), 
                                                 getString( node ), 
                                                 "injection can't be given an instance variable" );

        default:
            throw new IllegalInjectionException( node.getPosition(), 
                                                 getString( node ), 
                                                 "unknown node type" );
        }
    }

    protected static String getJavaClassName(Node node) {
        Node cur = node;

        Stack<String> stack = new Stack<String>();

        while (cur != null) {
            if (cur.getNodeType() == CALLNODE) {
                stack.push( ((CallNode) cur).getName() );
            } else if (cur.getNodeType() == VCALLNODE) {
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
    
    protected static boolean isValidInjectCall(FCallNode node) {
        String callName = node.getName();

        // check to make sure it's not a call to Enumerable#inject (it should
        // have a block (iter) in that case)
        return ("inject".equals( callName ) && null == node.getIterNode()) ||
                "__inject__".equals( callName );
    }

}
