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

package org.torquebox.web.websockets.component;

import org.apache.catalina.Session;
import org.jboss.logging.Logger;
import org.jboss.netty.channel.Channel;
import org.torquebox.core.component.AbstractRubyComponent;
import org.torquebox.web.websockets.WebSocketContext;

public class WebSocketProcessorComponent extends AbstractRubyComponent {

    public WebSocketProcessorComponent() {

    }

    public void setWebSocketContext(WebSocketContext context) {
        this.context = context;
    }

    public WebSocketContext getWebSocketContext() {
        return this.context;
    }

    public void setSession(Object session) {
        _callRubyMethodIfDefined( "session=", session );
    }
    
    public Object getSession() {
        return _callRubyMethodIfDefined( "session" );
    }
    
    public void setChannel(Channel channel) {
        _callRubyMethodIfDefined( "channel=", channel );
    }

    public void start() {
        _callRubyMethodIfDefined( "start" );
    }

    public void stop() {
        _callRubyMethodIfDefined( "start" );
    }

    public void connected() {
        _callRubyMethodIfDefined( "connected" );
    }

    public void disconnected() {
        _callRubyMethodIfDefined( "disconnected" );
    }
    
    public void on_message(String message) {
       _callRubyMethodIfDefined( "on_message", new Object[] { message } );
    }

    public void dispose() {
        this.context.releaseComponent( this );
    }


    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
    private WebSocketContext context;
    private Session session;
}
