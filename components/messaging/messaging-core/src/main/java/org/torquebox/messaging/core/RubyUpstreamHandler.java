package org.torquebox.messaging.core;

import org.jboss.logging.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.messaging.core.WebSocketsServer.TorqueBoxFrame;

/**
 * Upstream handler for vanilla websocket traffic. Receives a message and tries to invoke the proper Ruby handler.
 * @author mdobozy
 *
 */
public class RubyUpstreamHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = Logger.getLogger( RubyUpstreamHandler.class );

	private RubyComponentResolver componentResolver;

	private RubyRuntimePool rubyRuntimePool;

	private WebSocketsServer webSocketsServer;

	private String unitName;

	public void onInstall() {
		log.info( "Installing " + unitName + " ruby handler." );
		webSocketsServer.registerUUIDHandler( unitName, this );
		log.info( unitName + " ruby handler installed." );
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof TorqueBoxFrame) {
			TorqueBoxFrame frame = (TorqueBoxFrame) e.getMessage();
			if (frame.getType() == TorqueBoxFrame.DEFAULT_MEDIA_TYPE) {
				String data = new String( frame.getData(), "UTF-8" );
				Ruby ruby = rubyRuntimePool.borrowRuntime();
				IRubyObject processor = componentResolver.resolve( ruby );
				JavaEmbedUtils.invokeMethod( ruby, processor, "on_message", new Object[] { data }, void.class );
				rubyRuntimePool.returnRuntime( ruby );
			} // end if
		}
	}

	public void setComponentResolver(RubyComponentResolver componentResolver) {
		this.componentResolver = componentResolver;
	}

	public void setRubyRuntimePool(RubyRuntimePool rubyRuntimePool) {
		this.rubyRuntimePool = rubyRuntimePool;
	}

	public void setWebSocketsServer(WebSocketsServer webSocketsServer) {
		this.webSocketsServer = webSocketsServer;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

}
