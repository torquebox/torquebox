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

import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Decodes the HTTP session from the HTTP portion of the handshake.
 * 
 * <p>
 * Decodes the session for the new WebSockets connection. It scans for the
 * session in either an HTTP cookie, or passed through a <i>matrix parameter</i>
 * on the connection URL.
 * </p>
 * 
 * @author Bob McWhirter
 */
public class HttpSessionIdDecoder extends SimpleChannelUpstreamHandler {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String JSESSIONID = "JSESSIONID";

    public HttpSessionIdDecoder() {
        this( DEFAULT_SESSION_COOKIE_NAME );
    }

    public HttpSessionIdDecoder(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof HttpRequest) {
            String sessionId = decodeSessionIdFromCookie( (HttpRequest) e.getMessage() );
            if (sessionId == null) {
                sessionId = decodeSessionIdFromMatrix( (HttpRequest) e.getMessage() );
            }
            if ( sessionId != null ) {
                channelContext.sendUpstream( new SessionDecodedEvent( channelContext.getChannel(), sessionId ) );
            }
        }
        super.messageReceived( channelContext, e );
    }

    protected String decodeSessionIdFromCookie(HttpRequest message) {
        String cookieHeader = message.getHeader( Names.COOKIE );
        if (cookieHeader == null || cookieHeader.trim().equals( "" )) {
            return null;
        }
        CookieDecoder decoder = new CookieDecoder();
        Set<Cookie> cookies = decoder.decode( cookieHeader );
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase( this.sessionCookieName )) {
                return cookie.getValue();
            }
        }
        return null;
    }

    protected String decodeSessionIdFromMatrix(HttpRequest message) {
        String uri = message.getUri();

        int loc = uri.indexOf( ";" );

        if (loc < 0) {
            return null;
        }

        String matrix = uri.substring( loc + 1 );
        
        loc = matrix.indexOf(  "?"  );
        
        if ( loc >= 0 ) {
            matrix = matrix.substring( 0, loc );
        }
        
        StringTokenizer nameValuePairs = new StringTokenizer( matrix, "&" );

        while (nameValuePairs.hasMoreTokens()) {
            String nameValuePair = nameValuePairs.nextToken();

            int equalsLoc = nameValuePair.indexOf( "=" );

            if (equalsLoc >= 0) {
                String name = nameValuePair.substring( 0, equalsLoc );

                if (name.equalsIgnoreCase( JSESSIONID ) || name.equalsIgnoreCase( this.sessionCookieName )) {
                    String value = nameValuePair.substring( equalsLoc + 1 );
                    return value;
                }
            }
        }

        return null;
    }

    private String sessionCookieName;
}
