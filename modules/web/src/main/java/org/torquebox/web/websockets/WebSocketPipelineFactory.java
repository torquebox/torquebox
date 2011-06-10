package org.torquebox.web.websockets;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.torquebox.web.websockets.protocol.HandshakeHandler;

/**
 * Provides the Netty websocket pipeline factory.
 * 
 * @author mdobozy
 * 
 */
class WebSocketPipelineFactory implements ChannelPipelineFactory {
    
    public WebSocketPipelineFactory(ContextRegistry contextRegistry) {
        this.contextRegistry = contextRegistry;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();
        
        //pipeline.addLast(  "debug-A", new DebugHandler( "BORDER" )  );
        pipeline.addLast( "http-decoder", new HttpRequestDecoder() );
        pipeline.addLast( "http-aggregator", new HttpChunkAggregator( 65536 ) );
        pipeline.addLast( "http-encoder", new HttpResponseEncoder() );
        pipeline.addLast( "http-session-id-decoder", new HttpSessionIdDecoder() );
        //pipeline.addLast(  "debug-B", new DebugHandler( "TAIL" )  );
        
        pipeline.addLast( "websockets-handshake", new HandshakeHandler( this.contextRegistry ) );
        //pipeline.addLast(  "debug-C", new DebugHandler( "C" )  );
        
        return pipeline;
    }
    
    private ContextRegistry contextRegistry;


}