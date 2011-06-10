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

package org.torquebox.web.websockets;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.torquebox.web.websockets.protocol.HandshakeHandler;

/**
 * Registry for mapping incoming URIs to a particular <code>WebSocketContext</code>.
 * 
 * <p>
 * Used by the WebSocketsServer stack (specifically the {@link HandshakeHandler}) to route incoming requests to the
 * appropriate application-scoped context handler.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class ContextRegistry {

    /**
     * Construct.
     */
    public ContextRegistry() {

    }

    /**
     * Add a context to the registry.
     * 
     * <p>
     * Adding a context allows it to be matched through {@link #findContext(String, String)}.
     * </p>
     * 
     * @param context The context to add.
     */
    public void addContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for (String hostName : context.getHostNames()) {
            String key = makeKey( hostName, contextPath );
            log.info( "Link context: '" + key + "' to " + context );
            this.registry.put( key, context );
        }
    }

    /**
     * Remove a context from the registry.
     * 
     * <p>
     * Removing a context causes it to no longer be matched through {@link #findContext(String, String)}.
     * </p>
     * 
     * @param context The context to remove.
     */
    public void removeContext(WebSocketContext context) {
        String contextPath = context.getContextPath();
        for (String hostName : context.getHostNames()) {
            this.registry.remove( makeKey( hostName, contextPath ) );
        }
    }

    /**
     * Find the <code>WebSocketContext</code> that maps to the
     * provided URI components.
     * 
     * @param host The host-name receiving the request.
     * @param uri The absolute request URI
     * @return The matching <code>WebSocketContext</code> if any match the given inputs, otherwise <code>null</code>.
     */
    public WebSocketContext findContext(String host, String uri) {
        log.info( "Find context: " + makeKey( host, uri ) );
        return this.registry.get( makeKey( host, uri ) );
    }

    /**
     * Create a key used for indexing and lookups in the registry.
     * 
     * @param host The host name.
     * @param uri The URI.
     * @return The composite key.
     */
    protected static String makeKey(String host, String uri) {
        String hostWithoutPort = host;
        int colonLoc = host.indexOf( ":" );
        if (colonLoc >= 0) {
            hostWithoutPort = host.substring( 0, colonLoc );
        }

        return hostWithoutPort + ":" + uri;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
    
    /** The Host+URI mapping to WebSocketContexts. */
    private Map<String, WebSocketContext> registry = new HashMap<String, WebSocketContext>();

}
