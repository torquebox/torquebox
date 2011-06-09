package org.torquebox.web.websockets;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpSessionIdDecoder extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent e) throws Exception {
        if ( e.getMessage() instanceof HttpRequest ) {
            String sessionId = decodeSessionId( (HttpRequest) e.getMessage() );
            channelContext.setAttachment( sessionId );
        }
        super.messageReceived( channelContext, e );
    }

    protected String decodeSessionId(HttpRequest message) {
        return null;
    }

}
