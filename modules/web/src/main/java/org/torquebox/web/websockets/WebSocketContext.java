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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.jboss.as.web.VirtualHost;
import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

/**
 * Context for runtime management of a web-socket endpoint.
 * 
 * <p>
 * This object represents a live running endpoint bound to a context. The
 * lifecycle of the context (and thus the endpoint) is directly controlled on
 * instances of this class.
 * </p>
 * 
 * @author Bob McWhirter
 * 
 */
public class WebSocketContext {

    /**
     * Construct
     * 
     * @param name The unique-within-application-scope name of this context.
     * @param server The server responsible for this endpoint.
     * @param host The virtual host for this endpoint.
     * @param contextPath The full context path (including application context)
     *            of this endpoint.
     */
    WebSocketContext(String name, WebSocketsServer server, VirtualHost host, String contextPath) {
        this.name = name;
        this.server = server;
        this.host = host;
        this.contextPath = contextPath;
    }

    public String getName() {
        return this.name;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }

    public void setComponentResolver(ComponentResolver resolver) {
        this.resolver = resolver;
    }

    public ComponentResolver getComponentResolver() {
        return this.resolver;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Manager getManager() {
        return this.manager;
    }

    /**
     * Create a component instance, bound to an optional session.
     * 
     * <p>
     * The underlying handler component maps one-to-one with each connection.
     * Every connection gets its own instance of the underlying component for
     * the duration of the connection.
     * </p>
     * 
     * <p>
     * Given the potentially long-lived component, which consumes a Ruby
     * interpreter for its duration, care should be taken to ensure usage of a
     * shared pool with thread-safe code, or an appropriately tuned non-shared
     * pool.
     * </p>
     * 
     * @param session The web-container session, if available.
     * @return A component capable of processing requests for a specific
     *         connection, to this context.
     * @throws Exception If an error occurs while creating the underlying
     *             component.
     */
    public WebSocketProcessorComponent createComponent(Session session) throws Exception {
        Ruby runtime = this.runtimePool.borrowRuntime();
        WebSocketProcessorComponent component = (WebSocketProcessorComponent) this.resolver.resolve( runtime );
        component.setWebSocketContext( this );
        return component;
    }

    /**
     * Release a component (and its Ruby interpreter) from use.
     * 
     * @param component The component to dispose of.
     */
    public void releaseComponent(WebSocketProcessorComponent component) {
        Ruby runtime = component.getRubyComponent().getRuntime();
        this.runtimePool.returnRuntime( runtime );
    }

    /**
     * Retrieve all valid hostnames, including virtual hostnames, for this
     * context.
     * 
     * @return The list of all valid hostnames.
     */
    public List<String> getHostNames() {
        List<String> hostNames = new ArrayList<String>( 3 );
        hostNames.add( this.host.getHost().getName() );

        for (String name : this.host.getHost().findAliases()) {
            hostNames.add( name );
        }

        return hostNames;
    }

    /**
     * Locate a session given a session identifier.
     * 
     * <p>
     * The context shares the web-containers session store, with session
     * identifiers being valid between the two types of traffic.
     * </p>
     * 
     * @param sessionId The session identifier.
     * @return The session if found, otherwise <code>null</code>.
     * @throws IOException If the session store encounters errors while
     *             accessing its own backing data.
     */
    public Session findSession(String sessionId) throws IOException {
        log.info( "manager: " + this.manager );
        Session session = manager.findSession( sessionId );
        log.info( "session: " + session );
        return session;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Start this context.
     * 
     * <p>
     * Starting a context registers it with the server as a valid handler, and
     * may immediately begin processing requests if the server itself is
     * running.
     * </p
     * ?
     */
    public void start() {
        this.server.registerContext( this );
    }

    /**
     * Stop this context.
     * 
     * <p>
     * Stopping a context unregisters it with the server, and will immediately
     * cease processing requests. Existing in-flight connections are <b>not</b>
     * necessarily interrupted.
     * </p>
     */
    public void stop() {
        this.server.unregisterContext( this );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );

    private String name;

    /** The web-socket server responsible for hosting this context. */
    private WebSocketsServer server;
    
    /** The web-container session manager. */
    private Manager manager;

    private VirtualHost host;
    private String contextPath;

    private RubyRuntimePool runtimePool;
    private ComponentResolver resolver;

}
