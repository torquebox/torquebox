package org.torquebox.web.websockets.component;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;
import org.jboss.logging.Logger;
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
        _callIfDefined_WithSession( "start" );
        _callIfDefined_WithSession( "connected", event.getChannel() );
    }

    public void channelDisconnected(ChannelHandlerContext channelContext, ChannelStateEvent event) {
        _callIfDefined_WithSession( "disconnected", event.getChannel() );
        _callIfDefined_WithSession( "stop" );
    }

    public void dispose() {
        this.context.releaseComponent( this );
    }

    // TODO: Handle binary vs text conversions
    public WebSocketFrame handleMessage(ChannelHandlerContext channelContext, MessageEvent event) {
        log.info(  "on_message java component -> " + channelContext + "  " + event  );
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
            if (this.session != null) {
                session.access();
                log.info( "setting session" );
                _callRubyMethodIfDefined( "session=", this.session.getSession() );
                log.info( "completed setting session" );
            }
            log.info( "calling " + method );
            result = _callRubyMethodIfDefined( method, args );
            log.info( "called " + method );
        } finally {
            if (this.session != null) {
                log.info( "unsetting session" );
                _callRubyMethodIfDefined( "session=", new Object[] { null } );
                log.info( "completed unsetting session" );
                session.endAccess();
            }
        }

        return result;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
    private WebSocketContext context;
    private Session session;

}
