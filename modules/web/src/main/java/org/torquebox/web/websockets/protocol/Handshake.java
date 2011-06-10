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

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.torquebox.web.websockets.WebSocketContext;

/**
 * Abstraction of web-socket handshake versions.
 * 
 * <p>
 * Since each version uses different headers and behaves differently, these
 * differences are encapsulated in subclasses of <code>Handshake</code>.
 * </p>
 * 
 * @see HandshakeHandler
 * 
 * @author Bob McWhirter
 */
public abstract class Handshake {

    public Handshake(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    protected String getWebSocketLocation(WebSocketContext context, HttpRequest request) {
        return "ws://" + request.getHeader( HttpHeaders.Names.HOST ) + context.getContextPath();
    }

    public abstract boolean matches(HttpRequest request);

    public abstract HttpResponse generateResponse(WebSocketContext context, HttpRequest request) throws Exception;

    private String version;

}
