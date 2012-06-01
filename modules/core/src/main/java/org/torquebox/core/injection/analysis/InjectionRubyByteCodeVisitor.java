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

package org.torquebox.core.injection.analysis;

import static org.torquebox.core.injection.analysis.RubyInjectionUtils.getConstString;
import static org.torquebox.core.injection.analysis.RubyInjectionUtils.getString;
import static org.torquebox.core.injection.analysis.RubyInjectionUtils.isLegalInjection;
import static org.torquebox.core.injection.analysis.RubyInjectionUtils.isValidInjectCall;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.InjectionException;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.torquebox.core.analysis.DefaultNodeVisitor;

/**
 * Ruby byte-code visitor for injection analysis.
 * 
 * @author Bob McWhirter
 * @author Toby Crawley
 */
public class InjectionRubyByteCodeVisitor extends DefaultNodeVisitor {

    /**
     * Construct with an analyzer.
     * 
     * @param analyzer
     *            The analyzer.
     */
    public InjectionRubyByteCodeVisitor(InjectionAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Object visitFCallNode(FCallNode node) throws InjectionException {
        String callName = node.getName();

        if (!this.markerSeen && (callName.equals( "include" ) || callName.equals( "extend" ))) {
            Node argsNode = node.getArgsNode();
            if (argsNode instanceof ArrayNode) {
                String includedName = getConstString( ((ArrayNode) node.getArgsNode()).get( 0 ) );
                if (includedName.equals( TORQUEBOX_MARKER_MODULE )) {
                    this.markerSeen = true;
                }
            }
            else {
                log.debugf( "Ignoring non-array arg node for include/extend: %s", argsNode );
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
            } else if (callName.startsWith( OLD_INJECTION_PREFIX ) || callName.startsWith( INJECTION_PREFIX )) {
                String injectionType = callName.substring( INJECTION_PREFIX.length() );
                handler = this.analyzer.getInjectableHandlerRegistry().getHandler( injectionType );
                if (handler == null) {
                    throw new InvalidInjectionTypeException( node.getPosition(), injectionType );
                }
            }
            if (handler != null && isLegalInjection( node.getArgsNode() )) {
                Injectable injectable = handler.handle( node.getArgsNode(), generic );
                if (injectable != null) {
                    this.injectables.add( injectable );
                }
            } else {
                defaultVisitNode( node );
            }
        }

        return null;
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

    public void assumeMarkerSeen() {
        this.markerSeen = true;
    }

    public void reset() {
        this.markerSeen = false;
        this.injectables.clear();
    }

    private static final String INJECTION_PREFIX = "fetch_";
    private static final String OLD_INJECTION_PREFIX = "inject_";
    public static final String TORQUEBOX_MARKER_MODULE = "TorqueBox::Injectors";

    private InjectionAnalyzer analyzer;
    private boolean markerSeen = false;

    private Set<Injectable> injectables = new HashSet<Injectable>();

    private static final Logger log = Logger.getLogger( "org.torquebox.core.injection.analysis" );

}
