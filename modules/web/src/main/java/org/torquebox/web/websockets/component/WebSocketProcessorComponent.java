package org.torquebox.web.websockets.component;

import org.apache.catalina.Session;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
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
    
    public void setSession(Session session) {
        this.session = session;
    }
    
    public Session getSession() {
        return this.session;
    }

    public void channelConnected(ChannelHandlerContext channelContext, ChannelStateEvent event) {
        _callIfDefined_WithSession( "connected", event.getChannel() );
    }

    public void channelDisconnected(ChannelHandlerContext channelContext, ChannelStateEvent event) {
        _callIfDefined_WithSession( "disconnected", event.getChannel() );
    }
    
    public void dispose() {
        this.context.releaseComponent( this );
    }
    
    // TODO: Handle binary vs text conversions
    public WebSocketFrame handleMessage(ChannelHandlerContext channelContext, MessageEvent event) {
        WebSocketFrame frame = (WebSocketFrame) event.getMessage();
        String message = frame.getTextData();
        Object response = _callIfDefined_WithSession( "on_message", message );

        if (response != null) {
            return new DefaultWebSocketFrame( response.toString() );
        }
        return null;
    }

    protected Object _callIfDefined_WithSession(String method, Object... args) {
        Object result = null;

        try {
            session.access();
            _callRubyMethodIfDefined( "session=", this.session.getSession() );
            result = _callRubyMethodIfDefined( method, args );
        } finally {
            _callRubyMethodIfDefined( "session=", (Object[]) null );
            session.endAccess();
        }

        return result;
    }
    
    private WebSocketContext context;
    private Session session;

}
