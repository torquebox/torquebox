package org.torquebox.web.websockets;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;

public class SessionDecodedEvent implements ChannelEvent {

    

    public SessionDecodedEvent(Channel channel, String sessionId) {
        this.channel = channel;
        this.sessionId = sessionId;
    }
    
    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public ChannelFuture getFuture() {
        return null;
    }
    
    public String getSessionId() {
        return this.sessionId;
    }
    
    public String toString() {
        return "[SessionDecodeEvent: " + this.sessionId + "]";
    }
    
    private Channel channel;
    private String sessionId;

}
