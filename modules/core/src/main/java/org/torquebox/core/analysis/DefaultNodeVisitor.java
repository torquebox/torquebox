/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jruby.ast.*;
import org.jruby.ast.types.INameNode;
import org.jruby.ast.visitor.NodeVisitor;

public class DefaultNodeVisitor implements NodeVisitor {

    private Logger log = Logger.getLogger( this.getClass() );

    private int indentLevel = 0;

    private boolean trace = false;

    private static final int MAX_INDENT_LEVEL = 100;

    protected Object defaultVisitNode(Node node) {

        List<Object> results = new ArrayList<Object>();

        if (trace) {
            StringBuilder indent = new StringBuilder();

            if (!node.isInvisible()) {
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
                    Object childResult = child.accept( this );
                    if (childResult != null) {
                        results.add( childResult );
                    }
                } catch(UnsupportedOperationException ex) {
                    log.trace( "JRuby doesn't support visiting node " + child + " - skipping it, but looking at its children." );
                    for (Node grandChild : child.childNodes()) {
                        defaultVisitNode( grandChild );
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

    @Override
    public Object visitAliasNode(AliasNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitAndNode(AndNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitArgsNode(ArgsNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitArgsCatNode(ArgsCatNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitArgsPushNode(ArgsPushNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitArrayNode(ArrayNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitAttrAssignNode(AttrAssignNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBackRefNode(BackRefNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBeginNode(BeginNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBignumNode(BignumNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBlockArgNode(BlockArgNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBlockArg18Node(BlockArg18Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBlockNode(BlockNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBlockPassNode(BlockPassNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitBreakNode(BreakNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitConstDeclNode(ConstDeclNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitClassVarDeclNode(ClassVarDeclNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitClassVarNode(ClassVarNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitCallNode(CallNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitCaseNode(CaseNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitClassNode(ClassNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitColon2Node(Colon2Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitColon3Node(Colon3Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitConstNode(ConstNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDAsgnNode(DAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDRegxNode(DRegexpNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDStrNode(DStrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDSymbolNode(DSymbolNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDVarNode(DVarNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDXStrNode(DXStrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDefinedNode(DefinedNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDefnNode(DefnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDefsNode(DefsNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitDotNode(DotNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitEncodingNode(EncodingNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitEnsureNode(EnsureNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitEvStrNode(EvStrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitFCallNode(FCallNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitFalseNode(FalseNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitFixnumNode(FixnumNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitFlipNode(FlipNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitFloatNode(FloatNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitForNode(ForNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitGlobalVarNode(GlobalVarNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitHashNode(HashNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitInstAsgnNode(InstAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitInstVarNode(InstVarNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitIfNode(IfNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitIterNode(IterNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitLiteralNode(LiteralNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitLocalAsgnNode(LocalAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitLocalVarNode(LocalVarNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitMultipleAsgnNode(MultipleAsgn19Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitMatch2Node(Match2Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitMatch3Node(Match3Node iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitMatchNode(MatchNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitModuleNode(ModuleNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitNewlineNode(NewlineNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitNextNode(NextNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitNilNode(NilNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitNotNode(NotNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitNthRefNode(NthRefNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitOpAsgnNode(OpAsgnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitOrNode(OrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitPreExeNode(PreExeNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitPostExeNode(PostExeNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRedoNode(RedoNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRegexpNode(RegexpNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRescueBodyNode(RescueBodyNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRescueNode(RescueNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRestArgNode(RestArgNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRetryNode(RetryNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitReturnNode(ReturnNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitRootNode(RootNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSClassNode(SClassNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSelfNode(SelfNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSplatNode(SplatNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitStrNode(StrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSuperNode(SuperNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSValueNode(SValueNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitSymbolNode(SymbolNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitToAryNode(ToAryNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitTrueNode(TrueNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitUndefNode(UndefNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitUntilNode(UntilNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitVAliasNode(VAliasNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitVCallNode(VCallNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitWhenNode(WhenNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitWhileNode(WhileNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitXStrNode(XStrNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitYieldNode(YieldNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitZArrayNode(ZArrayNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    @Override
    public Object visitZSuperNode(ZSuperNode iVisited) {
        return defaultVisitNode( iVisited );
    }

    // No @Override annotation since this method only exists in
    // NodeVisitor under JRuby 1.7
    public Object visitLambdaNode(LambdaNode iVisited) {
        return defaultVisitNode( iVisited );
    }
}
