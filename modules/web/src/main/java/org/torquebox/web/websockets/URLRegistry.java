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

import org.jboss.as.server.deployment.AttachmentKey;

/**
 * Per-application registry mapping WebSocket endpoint names to concrete URLs.
 * 
 * <p>
 * Each web-socket endpoint is named and maps to a distinct URL on the server,
 * constructed from a combination of the application's primary hostname (if
 * multiple virtual-hosts are configured), the application's web context path,
 * and the endpoint's own context path. Additionally, since the web-socket TCP
 * server runs on a different port, its current port is also meaningful in the
 * construction of web-socket URLs.
 * </p>
 * 
 * <p>
 * When an endpoint starts, it adds its fully-qualified URL to the registry for
 * the application. When an endpoint stops, it removes itself from the registry.
 * </p>
 * 
 * <p>
 * Support is provided within Ruby applications through the
 * <code>TorqueBox::WebSockets</code> module, available in the
 * <code>torquebox-web</code> gem. This module provides a method
 * <code>lookup(name)</code> to retrieve the specific web-socket endpoint URL.
 * </p>
 * 
 * <pre>
 * @echo_ws_url = TorqueBox::WebSockets.lookup( 'echo' )
 * </pre>
 * 
 * @author Bob McWhirter
 */
public class URLRegistry {

    public static final AttachmentKey<URLRegistry> ATTACHMENT_KEY = AttachmentKey.create( URLRegistry.class );

    /** Construct. */
    public URLRegistry() {
    }

    /**
     * Lookup the URL associated with the given endpoint socket name.
     * 
     * @param socketName The name.
     * @return The associated fully-qualified URL if an endpoint with the given
     *         name is available, otherwise <code>null</code>.
     */
    public String lookupURL(String socketName) {
        System.err.println( "lookup: " + socketName );
        return this.urls.get( socketName );
    }

    /**
     * Register a URL for a named endpoint.
     * 
     * @param socketName The endpoint socket name.
     * @param url The fully-qualified URL.
     */
    public void registerURL(String socketName, String url) {
        System.err.println( "registering: " + socketName );
        System.err.println( "  url: " + url );
        this.urls.put( socketName, url );
    }

    private final Map<String, String> urls = new HashMap<String, String>();
}
