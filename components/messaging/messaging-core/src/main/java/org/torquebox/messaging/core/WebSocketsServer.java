package org.torquebox.messaging.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.util.VirtualExecutorService;

public class WebSocketsServer {

	private static final Logger log = Logger.getLogger( WebSocketsServer.class );

	private ServerBootstrap bootstrap;

	private Map<Integer, String> connections;

	private String context;

	private Map<Integer, MediaHandlerMetaData> mediaHandlers;

	private VirtualExecutorService parentExecutor;

	private int port;

	private ExecutorService threadPool;

	private Map<String, ChannelUpstreamHandler> uuidHandlers;

	public String getContext() {
		return context;
	}

	public void onInstall() {
		log.info( "Initializing TorqueBox web sockets server [context = " + context + ", port = " + port + "]" );
		this.initialize();
		bootstrap.setOption( "reuseAddress", true );
		bootstrap.setPipelineFactory( new WebSocketsPipelineFactory() );
		bootstrap.bind( new InetSocketAddress( port ) );
		log.info( "Initialization complete." );
	}

	public void onUninstall() {
		log.info( "Stopping TorqueBox web sockets server." );
		// TODO: shut 'er down.
	}

	public void registerMediaHandler(int mediaType, Class<? extends WebSocketsMediaUpstreamHandler> handler,
			Map<String, Object> configs) {
		mediaHandlers.put( mediaType, new MediaHandlerMetaData( handler, configs ) );
	}

	public void registerUUIDHandler(String uuid, ChannelUpstreamHandler handler) {
		uuidHandlers.put( uuid, handler );
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private void initialize() {
		this.connections = new HashMap<Integer, String>( 100 );
		this.uuidHandlers = new HashMap<String, ChannelUpstreamHandler>( 10 );
		this.mediaHandlers = new HashMap<Integer, MediaHandlerMetaData>( 10 );
		this.threadPool = Executors.newCachedThreadPool();
		this.parentExecutor = new VirtualExecutorService( threadPool );
		ExecutorService workerExecutor = new VirtualExecutorService( threadPool );
		NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory( parentExecutor, workerExecutor );
		bootstrap = new ServerBootstrap( factory );

		Map<String, Object> configs = new HashMap<String, Object>( 3 );
		configs.put( "host", "localhost" );
		configs.put( "port", 61613 );
		configs.put( "type", (byte) 0x02 );
		this.registerMediaHandler( 0x02, StompProxyUpstreamHandler.class, configs );

	}

	public static class MediaHandlerMetaData {

		private Class<? extends WebSocketsMediaUpstreamHandler> mediaHandler;
		private Map<String, Object> configuration;

		public MediaHandlerMetaData(Class<? extends WebSocketsMediaUpstreamHandler> handler, Map<String, Object> configs) {
			this.mediaHandler = handler;
			this.configuration = configs;
		}

	}

	private class WebSocketsPipelineFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = new DefaultChannelPipeline();
			pipeline.addLast( "decoder", new HttpRequestDecoder() );
			pipeline.addLast( "aggregator", new HttpChunkAggregator( 65536 ) );
			pipeline.addLast( "encoder", new HttpResponseEncoder() );
			pipeline.addLast( "websockets-handler", new WebSocketsServerHandler() );
			pipeline.addLast( "torquebox-encoder", new TorqueBoxFrameEncoder() );
			pipeline.addLast( "torquebox-frame-handler", new TorqueBoxFrameHandler() );
			return pipeline;
		}

	}

	/**
	 * Transforms a WebSocketFrame into a TorqueBoxFrame
	 * 
	 * @author mdobozy
	 * 
	 */
	private class TorqueBoxFrameHandler extends SimpleChannelUpstreamHandler {

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
			int channelId = ctx.getChannel().getId();
			String removedId = connections.remove( channelId );
			if (removedId == null)
				log.warn( "Attempt to remove connection " + channelId + ", which does not exist." );
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

			if (!(e.getMessage() instanceof WebSocketFrame)) {
				Channels.fireMessageReceived( ctx.getChannel(), e.getMessage() );
				return;
			}

			WebSocketFrame wsFrame = (WebSocketFrame) e.getMessage();
			ChannelBuffer buffer = wsFrame.getBinaryData();
			byte contentType = buffer.readByte();
			byte commandType = buffer.readByte();
			byte[] data = buffer.readBytes( buffer.readableBytes() ).array();

			if (commandType == TorqueBoxFrame.HANDSHAKE_COMMAND) {
				connections.put( ctx.getChannel().getId(), new String( data, "UTF-8" ) );
				TorqueBoxFrame frame = new TorqueBoxFrame( contentType, TorqueBoxFrame.HANDSHAKE_RESPONSE_COMMAND, data );
				ctx.getChannel().write( frame ).awaitUninterruptibly();
			} else {
				TorqueBoxFrame frame = new TorqueBoxFrame( contentType, TorqueBoxFrame.NORMAL_COMMAND, data );
				Channel channel = ctx.getChannel();
				ChannelPipeline pipeline = channel.getPipeline();
				if (contentType == TorqueBoxFrame.DEFAULT_MEDIA_TYPE) {
					if (pipeline.get( "uuid-handler" ) == null) {
						String uuid = connections.get( ctx.getChannel().getId() );
						log.debug( "Looking for handler for UUID " + uuid );
						ChannelUpstreamHandler rubyHandler = uuidHandlers.get( uuid );
						channel.getPipeline().addLast( "uuid-handler", rubyHandler );
					} // end if (we don't have a ruby handler defined for this channel yet)
					ChannelEvent event = new UpstreamMessageEvent( channel, frame, channel.getRemoteAddress() );
					ctx.sendUpstream( event );
				} else if (channel.getPipeline().get( "media-handler" ) == null) {
					MediaHandlerMetaData metaData = mediaHandlers.get( (int) contentType );
					if (metaData != null) {
						log.debug( "Found media handler for content type " + contentType );
						WebSocketsMediaUpstreamHandler mediaHandler = metaData.mediaHandler.newInstance();
						mediaHandler.configure( metaData.configuration );
						pipeline.addLast( "media-handler", (ChannelUpstreamHandler) mediaHandler );
						ChannelBuffer mediaBuffer = channel.getConfig().getBufferFactory()
								.getBuffer( frame.getData().length );
						mediaBuffer.writeBytes( frame.getData() );
						ChannelEvent event = new UpstreamMessageEvent( channel, mediaBuffer, channel.getRemoteAddress() );
						ctx.sendUpstream( event );
					} else
						ctx.sendUpstream( e );
				} else {
					ChannelBuffer mediaBuffer = channel.getConfig().getBufferFactory()
							.getBuffer( frame.getData().length );
					mediaBuffer.writeBytes( frame.getData() );
					ChannelEvent event = new UpstreamMessageEvent( channel, mediaBuffer, ctx.getChannel()
							.getRemoteAddress() );
					ctx.sendUpstream( event );
				}
			}
		}
	}

	/**
	 * Transforms a TorqueBoxFrame into a WebSocketFrame.
	 * 
	 * @author mdobozy
	 * 
	 */
	private class TorqueBoxFrameEncoder extends OneToOneEncoder {

		@Override
		protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
			Object msgToReturn = msg;
			if (msg instanceof TorqueBoxFrame) {
				TorqueBoxFrame frame = (TorqueBoxFrame) msg;
				byte[] data = frame.getData();
				WebSocketFrame wsFrame = new DefaultWebSocketFrame();

				ChannelBuffer buffer = channel.getConfig().getBufferFactory().getBuffer( data.length + 2 );
				buffer.writeByte( frame.getType() );
				buffer.writeByte( frame.getCommand() );
				buffer.writeBytes( data );
				wsFrame.setData( 0, buffer );
				msgToReturn = wsFrame;
			}
			return msgToReturn;
		}
	}

	public static class TorqueBoxFrame {

		public static byte DEFAULT_MEDIA_TYPE = 0x01;

		public static byte HANDSHAKE_COMMAND = 0x01;
		public static byte HANDSHAKE_RESPONSE_COMMAND = 0x02;
		public static byte NORMAL_COMMAND = 0x03;

		private byte type;
		private byte command;
		private byte[] data;

		public TorqueBoxFrame(byte type, byte command, byte[] data) {
			this.type = type;
			this.command = command;
			this.data = data;
		}

		public byte getCommand() {
			return command;
		}

		public byte getType() {
			return type;
		}

		public byte[] getData() {
			return data;
		}

	}

}
