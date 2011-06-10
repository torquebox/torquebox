/*
 * Copyright 2010 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
        digester.update(  inputArray, 0, len );
        byte[] hash = digester.digest();
        response.setContent( ChannelBuffers.wrappedBuffer( hash ) );
        
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