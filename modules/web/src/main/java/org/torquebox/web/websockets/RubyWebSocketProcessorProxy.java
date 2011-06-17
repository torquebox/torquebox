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

import org.apache.catalina.Session;
import org.jboss.logging.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

/**
 * Bridge between Netty and the Java side of the Ruby handler.
 * 
 * <p>
 * This handler is attached to the tail of the Netty channel pipeline to
 * dispatch {@link WebSocketFrame}s to the underlying component.
 * </p>
 * 
 * @author Michael Dobozy
 * @author Bob McWhirter
 * 
 */
public class RubyWebSocketProcessorProxy extends SimpleChannelUpstreamHandler {

    /**
     * Construct around a session and component.
     * 
     * @param component
     */
    public RubyWebSocketProcessorProxy(Session session, WebSocketProcessorComponent component) {
        this.session = session;
        this.component = component;
    }

    protected Object getRubySession() {

        Ruby ruby = this.component.getRubyComponent().getRuntime();

        if (this.session != null) {
            RubyModule servletStoreClass = ruby.getClassFromPath( "TorqueBox::Session::ServletStore" );
            this.rubySession = JavaEmbedUtils.invokeMethod( ruby, servletStoreClass, "load_session_data", new Object[] { this.session }, Object.class );
        } else {
            RubyModule sessionDataClass = ruby.getClassFromPath( "TorqueBox::Session::SessionData" );
            this.rubySession = JavaEmbedUtils.invokeMethod( ruby, sessionDataClass, "new", new Object[] {}, Object.class );
        }

        return this.rubySession;
    }

    protected void commitSession() {
        if (this.session != null) {
            System.err.println( "Committing session" );
            Ruby ruby = this.component.getRubyComponent().getRuntime();
            RubyModule servletStoreClass = ruby.getClassFromPath( "TorqueBox::Session::ServletStore" );
            JavaEmbedUtils.invokeMethod( ruby, servletStoreClass, "store_session_data", new Object[] { this.session, this.rubySession }, void.class );
            System.err.println( "Committed session" );
        }
    }

    protected void beginSessionAccess() {
        if (this.session != null) {
            this.session.access();
        }
    }

    protected void endSessionAccess() {
        if (this.session != null) {
            this.session.endAccess();
        }
    }

    @Override
    public void channelConnected(ChannelHandlerContext channelContext, ChannelStateEvent event) throws Exception {
        this.component.start();
        beginSessionAccess();
        try {
            this.component.setChannel( event.getChannel() );
            this.component.setSession( getRubySession() );
            this.component.connected();
            commitSession();
        } finally {
            endSessionAccess();
        }
        super.channelConnected( channelContext, event );
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext channelContext, ChannelStateEvent event) throws Exception {
        try {
            beginSessionAccess();
            try {
                this.component.disconnected();
                this.component.setSession( null );
                commitSession();
            } finally {
                endSessionAccess();
            }
        } finally {
            this.component.dispose();
        }
        super.channelDisconnected( channelContext, event );
    }

    @Override
    public void messageReceived(ChannelHandlerContext channelContext, MessageEvent event) throws Exception {
        if (event.getMessage() instanceof WebSocketFrame) {
            String message = ((WebSocketFrame) event.getMessage()).getTextData();
            beginSessionAccess();
            try {
                this.component.on_message( message );
                commitSession();
            } finally {
                endSessionAccess();
            }
        }
        super.messageReceived( channelContext, event );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets.protocol" );

    private Session session;
    private Object rubySession;

    private WebSocketProcessorComponent component;
}
