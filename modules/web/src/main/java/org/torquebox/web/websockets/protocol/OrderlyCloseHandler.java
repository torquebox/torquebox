package org.torquebox.web.websockets.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

public class OrderlyCloseHandler extends SimpleChannelUpstreamHandler {
    
    private ChannelBuffer CLOSE_PACKET = ChannelBuffers.wrappedBuffer( new byte[] { (byte) 0xFF, (byte) 0x00 } );

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if ( e.getMessage() instanceof WebSocketFrame ) {
            WebSocketFrame frame = (WebSocketFrame) e.getMessage();
            if ( frame.getType() == 255 ) {
                if ( frame.getBinaryData().readableBytes() == 0 ) {
                    ChannelFuture future = e.getChannel().write( CLOSE_PACKET );
                    future.addListener( ChannelFutureListener.CLOSE );
                    return;
                }
            }
            
        }
        super.messageReceived( ctx, e );
    }

    
}
