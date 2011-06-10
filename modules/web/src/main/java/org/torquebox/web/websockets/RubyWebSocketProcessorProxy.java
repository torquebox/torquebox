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

import org.jboss.logging.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

/**
 * Bridge between Netty and the Java side of the Ruby handler.
 * 
 * <p>This handler is attached to the tail of the Netty channel
 * pipeline to dispatch {@link WebSocketFrame}s to the underlying
 * component.</p>
 * 
 * @author Michael Dobozy
 * @author Bob McWhirter
 * 
 */
public class RubyWebSocketProcessorProxy extends SimpleChannelUpstreamHandler {

    
    /** Construct around a component.
     * 
     * @param component
     */
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
        log.info( "on_message java netty -> " + channelContext + "  " + event );
        if (event.getMessage() instanceof WebSocketFrame) {
            Object response = this.component.handleMessage( channelContext, event );
            if (response != null) {
                event.getChannel().write( response );
                return;
            }
            super.messageReceived( channelContext, event );
        }
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets.protocol" );
    private WebSocketProcessorComponent component;
}
