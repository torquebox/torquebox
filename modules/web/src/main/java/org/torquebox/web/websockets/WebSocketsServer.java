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
 * The Netty-based web sockets server.
 * 
 * @author mdobozy
 * 
 */
public class WebSocketsServer {

    public WebSocketsServer(int port) {
        this.contextRegistry = new ContextRegistry();
    }

    public int getPort() {
        return this.port;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return this.executor;
    }
    
    public void registerContext(WebSocketContext context) {
        this.contextRegistry.addContext( context );
    }
    
    public void unregisterContext(WebSocketContext context) {
        this.contextRegistry.removeContext( context );
    }

    public void start() {
        ServerBootstrap bootstrap = createServerBootstrap();
        this.channel = bootstrap.bind( new InetSocketAddress( port ) );
        log.info( "Initialization complete." );
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
