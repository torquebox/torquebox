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

import java.security.MessageDigest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.torquebox.web.websockets.WebSocketContext;

/**
 * Handler for ietf-00.
 * 
 * @author Michael Dobozy
 * @author Bob McWhirter
 * 
 */
public class Handshake_Ietf00 extends Handshake {

    public Handshake_Ietf00() {
        super( "0" );
    }

    public boolean matches(HttpRequest request) {
        return (request.containsHeader( Names.SEC_WEBSOCKET_KEY1 ) && request.containsHeader( Names.SEC_WEBSOCKET_KEY2 ));
    }

    @Override
    public HttpResponse generateResponse(WebSocketContext context, HttpRequest request) throws Exception {
        HttpResponse response = new DefaultHttpResponse( HttpVersion.HTTP_1_1, new HttpResponseStatus( 101, "Web Socket Protocol Handshake - IETF-00" ) );

        response.addHeader( Names.SEC_WEBSOCKET_ORIGIN, request.getHeader( Names.ORIGIN ) );
        response.addHeader( Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation( context, request ) );

        String protocol = request.getHeader( Names.SEC_WEBSOCKET_PROTOCOL );

        if (protocol != null) {
            response.addHeader( Names.SEC_WEBSOCKET_PROTOCOL, protocol );
        }

        // Calculate the answer of the challenge.
        String key1 = request.getHeader( Names.SEC_WEBSOCKET_KEY1 );
        String key2 = request.getHeader( Names.SEC_WEBSOCKET_KEY2 );
        long a = solve( key1 );
        long b = solve( key2 );

        byte[] c = new byte[8];
        request.getContent().readBytes( c );

        ChannelBuffer input = ChannelBuffers.buffer( 64 );
        input.writeBytes( ("" + a).getBytes() );
        input.writeBytes( ("" + b).getBytes() );
        input.writeBytes( c );

        byte[] inputArray = input.array();

        int len = input.readableBytes();
        MessageDigest digester = MessageDigest.getInstance( "MD5" );
        digester.update( inputArray, 0, len );
        byte[] hash = digester.digest();
        response.setContent( ChannelBuffers.wrappedBuffer( ChannelBuffers.wrappedBuffer( hash ), ChannelBuffers.wrappedBuffer( new byte[]{ '\r', '\n' } ) ) );

        return response;
    }

    protected long solve(String key) {
        String digitsOnly = key.replaceAll( "[^0-9]", "" );
        long digits = Long.parseLong( digitsOnly );
        long spaces = key.replaceAll( "[^ ]", "" ).length();
        long solution = digits / spaces;
        return solution;
    }
}