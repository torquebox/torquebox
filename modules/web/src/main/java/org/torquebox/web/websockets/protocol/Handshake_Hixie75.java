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

package org.torquebox.web.websockets.protocol;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.torquebox.web.websockets.WebSocketContext;

/**
 * Handler for hixie-75.
 * 
 * @author Michael Dobozy
 * @author Bob McWhirter
 */
public class Handshake_Hixie75 extends Handshake {

    public Handshake_Hixie75() {
        super( "hixie-75" );
    }

    public boolean matches(HttpRequest request) {
        return true;
    }

    @Override
    public HttpResponse generateResponse(WebSocketContext context, HttpRequest request) throws Exception {
        HttpResponse response = new DefaultHttpResponse( HttpVersion.HTTP_1_1, new HttpResponseStatus( 101, "Web Socket Protocol Handshake - Hixie-75" ) );

        response.addHeader( Names.WEBSOCKET_ORIGIN, request.getHeader( Names.ORIGIN ) );
        response.addHeader( Names.WEBSOCKET_LOCATION, getWebSocketLocation( context, request ) );
        String protocol = request.getHeader( Names.WEBSOCKET_PROTOCOL );
        if (protocol != null) {
            response.addHeader( Names.WEBSOCKET_PROTOCOL, protocol );
        }

        return response;
    }
}