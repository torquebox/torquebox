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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.torquebox.web.websockets.protocol.HandshakeHandler;

/**
 * Factory for the initial web-socket pipeline factory.
 * 
 * <p>
 * This factory produces a unique pipeline for each new connection since the
 * protocol is dynamic. The protocol causes the pipeline to reconfigure itself,
 * and thus cannot be shared between connections.
 * </p>
 * 
 * @author Michael Dobozy
 */
class WebSocketPipelineFactory implements ChannelPipelineFactory {

    /**
     * Construct with a context registry.
     * 
     * <p>The context-registry is required for the pipeline to
     * be able to dispatch connections to the correct application's
     * endpoint.
     * 
     * @param contextRegistry The registry.
     */
    public WebSocketPipelineFactory(ContextRegistry contextRegistry) {
        this.contextRegistry = contextRegistry;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = new DefaultChannelPipeline();

        pipeline.addLast( "http-decoder", new HttpRequestDecoder() );
        pipeline.addLast( "http-aggregator", new HttpChunkAggregator( 65536 ) );
        pipeline.addLast( "http-encoder", new HttpResponseEncoder() );
        pipeline.addLast( "http-session-id-decoder", new HttpSessionIdDecoder() );

        pipeline.addLast( "websockets-handshake", new HandshakeHandler( this.contextRegistry ) );

        return pipeline;
    }

    private ContextRegistry contextRegistry;

}