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

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.torquebox.web.websockets.WebSocketContext;

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