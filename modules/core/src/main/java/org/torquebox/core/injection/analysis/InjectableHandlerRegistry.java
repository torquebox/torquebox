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

package org.torquebox.core.injection.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/** Registry of handlers for the content of inject(...) calls.
 * 
 * <p>Handlers are added to the registry through the Java service loader
 * mechanism.</p>
 * 
 * <p>To add a handler, package it in a jar with file named 
 * <code>META-INF/org.torquebox.core.injection.analysis.InjectableHandler</code>
 * whose contents is the names (1 per line) of {@link InjectableHandler} implementations.</p>
 * 
 * @see InjectableHandler
 * 
 * @author Bob McWhirter
 */
public class InjectableHandlerRegistry implements Service<InjectableHandlerRegistry> {
    
    public InjectableHandlerRegistry() {
    }
    
    public void addInjectableHandler(InjectableHandler handler) {
        this.registry.put( handler.getType(), handler );
        this.handlersByPriority.add(  handler  );
    }
    
    /** Retrieve a handler by its registered type.
     * 
     * @param type The type.
     * @return The handler, or <code>null</code> if none match the type.
     */
    public InjectableHandler getHandlerForType(String type) {
        return this.registry.get( type );
    }

    /** Retrieve a handler by its compatibility with the argument.
     * 
     * @param injection The injection string.
     * @return The handler or <code>null</code> if none are compatible.
     */
    public InjectableHandler getHandler(Object injection) {
        for ( InjectableHandler each : this.handlersByPriority ) {
            if ( each.recognizes( injection ) ) {
                return each;
            }
        }
        
        return null;
    }
    
    /** Retrieve the full set of predetermined <code>Injectable</code>.
     * 
     * @return The predetermined injectables.
     */
    public Set<Injectable> getPredeterminedInjectables() {
        Set<Injectable> injectables = new HashSet<Injectable>();
        for (InjectableHandler each : this.handlersByPriority ) {
            if ( each instanceof PredeterminedInjectableHandler ) {
                injectables.addAll( ((PredeterminedInjectableHandler)each).getInjectables());
            }
        }
        return injectables;
    }

    @Override
    public InjectableHandlerRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.core.injection.analysis" );

    /** The underlying registry, by name. */
    private Map<String, InjectableHandler> registry = new HashMap<String, InjectableHandler>();
    
    /** The handlers, sorted by priority. */
    private TreeSet<InjectableHandler> handlersByPriority = new TreeSet<InjectableHandler>( InjectableHandler.RECOGNITION_PRIORITY );
}
