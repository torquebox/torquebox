package org.torquebox.web.websockets;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

public class RubyWebSocketProcessorProxy extends SimpleChannelUpstreamHandler {

    public RubyWebSocketProcessorProxy(WebSocketProcessorComponent component) {
        this.component = component;
    }

    @Override
    public void channelConnected(ChannelHandlerContext channelContext, ChannelStateEvent event) throws Exception {
        this.component.channelConnected( channelContext, event );
        super.channelConnected( channelContext, event );
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext channelContext, ChannelStateEvent event) throws Exception {
        try {
            this.component.channelDisconnected( channelContext, event );
            super.channelDisconnected( channelContext, event );
        } finally {
            this.component.dispose();
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent event) throws Exception {
        if (event.getMessage() instanceof WebSocketFrame) {
            Object response = this.component.handleMessage( channelContext, event );
            if (response != null) {
                event.getChannel().write( response );
                return;
            }
            super.messageReceived( channelContext, event );
        }
    }

    private WebSocketProcessorComponent component;
}
