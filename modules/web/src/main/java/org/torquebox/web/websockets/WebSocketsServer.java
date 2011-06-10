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

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.jboss.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.VirtualExecutorService;

/**
 * Netty-based web-sockets server.
 * 
 * <p>
 * This server is typically shared by all applications within an AS, similar to
 * the web container. It runs on a single port, distinct from regular HTTP
 * traffic.
 * </p>
 * 
 * <p>
 * The server supports dispatching based on both virtual-host and context-path
 * information provided in the initial handshake, allowing it to parallel the
 * web-container semantics.
 * </p>
 * 
 * @author Michael Dobozy
 * @author Bob McWhirter
 */
public class WebSocketsServer {

    /**
     * Construct with a port.
     * 
     * @param port The listen port to bind to.
     */
    public WebSocketsServer(int port) {
        this.contextRegistry = new ContextRegistry();
        this.port = port;
    }

    /**
     * Retrieve the bind port.
     * 
     * @return The bind port.
     */
    public int getPort() {
        return this.port;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return this.executor;
    }

    /**
     * Register a <code>WebSocketContext</code> with this server.
     * 
     * <p>
     * Registering a context allows it to service connections requests matching
     * the context's host and path.
     * </p>
     * 
     * @param context The context to register.
     */
    public void registerContext(WebSocketContext context) {
        this.contextRegistry.addContext( context );
    }

    /**
     * Unregister a <code>WebSocketContext</code> with this server.
     * 
     * @param context
     */
    public void unregisterContext(WebSocketContext context) {
        this.contextRegistry.removeContext( context );
    }

    /**
     * Start this server.
     * 
     */
    public void start() {
        ServerBootstrap bootstrap = createServerBootstrap();
        this.channel = bootstrap.bind( new InetSocketAddress( this.port ) );
        log.info( "WebSockets server on port: " + this.port );
    }

    protected ServerBootstrap createServerBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap( createChannelFactory() );
        bootstrap.setOption( "reuseAddress", true );
        bootstrap.setPipelineFactory( new WebSocketPipelineFactory( this.contextRegistry ) );
        return bootstrap;
    }

    protected ServerSocketChannelFactory createChannelFactory() {
        VirtualExecutorService bossExecutor = new VirtualExecutorService( this.executor );
        VirtualExecutorService workerExecutor = new VirtualExecutorService( this.executor );
        return new NioServerSocketChannelFactory( bossExecutor, workerExecutor );
    }

    /**
     * Stop this server.
     */
    public void stop() {
        this.channel.close();
        this.channel = null;
    }

    private static final Logger log = Logger.getLogger( WebSocketsServer.class );

    private ContextRegistry contextRegistry;

    private int port;

    private Executor executor;
    private Channel channel;

}
