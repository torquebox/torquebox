package org.torquebox.messaging.core;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.torquebox.messaging.core.WebSocketsServer.TorqueBoxFrame;

/**
 * The purpose of this handler is to proxy websocket traffic to the STOMP
 * acceptor running on HornetQ.
 * 
 * @author mdobozy
 * 
 */
public class StompProxyUpstreamHandler extends SimpleChannelUpstreamHandler implements WebSocketsMediaUpstreamHandler {

	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	static void closeOnFlush(Channel ch) {
		if (ch.isConnected()) {
			ch.write( ChannelBuffers.EMPTY_BUFFER ).addListener( ChannelFutureListener.CLOSE );
		}
	}

	private volatile Channel outboundChannel;

	private String stompHost;

	private int stompPort;

	private byte stompType;

	// This lock guards against the race condition that overrides the
	// OP_READ flag incorrectly.
	// See the related discussion: http://markmail.org/message/x7jc6mqx6ripynqf
	final Object trafficLock = new Object();

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (outboundChannel != null) {
			closeOnFlush( outboundChannel );
		}
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// If inboundChannel is not saturated anymore, continue accepting
		// incoming traffic from the outboundChannel.
		if (outboundChannel != null) {
			synchronized (trafficLock) {
				if (e.getChannel().isWritable()) {
					outboundChannel.setReadable( true );
				}
			}
		}
	}

	@Override
	public void configure(Map<String, Object> params) {
		this.stompHost = (String) params.get( "host" );
		this.stompPort = (Integer) params.get( "port" );
		this.stompType = (Byte) params.get( "type" );
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		closeOnFlush( e.getChannel() );
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		ChannelFuture f = this.initProxy( ctx, e );
		if (f != null) {
			f.addListener( new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					ChannelBuffer msg = (ChannelBuffer) e.getMessage();
					synchronized (trafficLock) {
						outboundChannel.write( msg );
						// If outboundChannel is saturated, do not read until
						// notified in
						// OutboundHandler.channelInterestChanged().
						if (!outboundChannel.isWritable()) {
							e.getChannel().setReadable( false );
						}
					}
				}

			} );
		} else {
			outboundChannel.write( e.getMessage() );
			if (!outboundChannel.isWritable())
				e.getChannel().setReadable( false );

		}
	}

	public void setStompHost(String stompHost) {
		this.stompHost = stompHost;
	}

	public void setStompPort(int stompPort) {
		this.stompPort = stompPort;
	}

	private ChannelFuture initProxy(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

		if (outboundChannel == null) {
			
			// Suspend incoming traffic until connected to the remote host.
			final Channel inboundChannel = e.getChannel();
			inboundChannel.setReadable( false );			
			
			// Start the connection attempt.
			ExecutorService executor = Executors.newCachedThreadPool();
			ClientSocketChannelFactory factory = new NioClientSocketChannelFactory( executor, executor );
			org.jboss.netty.bootstrap.ClientBootstrap cb = new ClientBootstrap( factory );
			cb.getPipeline().addLast( "handler", new OutboundHandler( e.getChannel() ) );
			ChannelFuture f = cb.connect( new InetSocketAddress( stompHost, stompPort ) );

			outboundChannel = f.getChannel();
			f.addListener( new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						// Connection attempt succeeded: accept incoming
						// traffic.
						inboundChannel.setReadable( true );
					} else {
						// Close the connection if the connection attempt has
						// failed.
						inboundChannel.close();
					}
				}
			} );
			return f;
		} // end if
		return null;
	}

	private class OutboundHandler extends SimpleChannelUpstreamHandler {

		private final Channel inboundChannel;

		OutboundHandler(Channel inboundChannel) {
			this.inboundChannel = inboundChannel;
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			closeOnFlush( inboundChannel );
		}

		@Override
		public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			// If outboundChannel is not saturated anymore, continue accepting
			// the incoming traffic from the inboundChannel.
			synchronized (trafficLock) {
				if (e.getChannel().isWritable()) {
					inboundChannel.setReadable( true );
				}
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			e.getCause().printStackTrace();
			closeOnFlush( e.getChannel() );
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
			ChannelBuffer msg = (ChannelBuffer) e.getMessage();
			synchronized (trafficLock) {
				TorqueBoxFrame frame = new TorqueBoxFrame( stompType, TorqueBoxFrame.NORMAL_COMMAND, msg.array() );
				inboundChannel.write( frame );

				// If inboundChannel is saturated, do not read until notified.
				if (!inboundChannel.isWritable()) {
					e.getChannel().setReadable( false );
				}
			}
		}
	}
}