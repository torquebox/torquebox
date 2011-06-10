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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * <b>NOT YET IMPLEMENTED</b> Decodes the HTTP session from the HTTP portion of the handshake.
 * 
 * <p><b>NOT YET IMPLEMENTED</b></p>
 * 
 * <p>
 * Decodes the session for the new WebSockets connection.  It scans for the
 * session in either an HTTP cookie, or passed through a <i>matrix parameter</i>
 * on the connection URL.
 * </p>
 * 
 * <p><b>NOT YET IMPLEMENTED</b></p>
 * 
 * @author Bob McWhirter
 */
public class HttpSessionIdDecoder extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof HttpRequest) {
            String sessionId = decodeSessionId( (HttpRequest) e.getMessage() );
            channelContext.setAttachment( sessionId );
        }
        super.messageReceived( channelContext, e );
    }

    protected String decodeSessionId(HttpRequest message) {
        return null;
    }

}
