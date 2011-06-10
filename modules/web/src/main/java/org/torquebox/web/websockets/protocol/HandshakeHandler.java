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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.Session;
import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.torquebox.web.websockets.ContextRegistry;
import org.torquebox.web.websockets.RubyWebSocketProcessorProxy;
import org.torquebox.web.websockets.WebSocketContext;
import org.torquebox.web.websockets.component.WebSocketProcessorComponent;

/**
 * @author mdobozy Based on Trustin Lee's example WebSocketServerListener.
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * 
 * @version $Rev: 2314 $, $Date: 2010-06-22 09:02:27 +0200 (Mar, 22 jui 2010) $
 */
public class HandshakeHandler extends SimpleChannelUpstreamHandler {

    public HandshakeHandler(ContextRegistry contextRegistry) {
        this.contextRegistry = contextRegistry;
        this.handshakes.add( new Handshake_Ietf00() );
        this.handshakes.add( new Handshake_Hixie75() );
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest( ctx, (HttpRequest) msg );
        } else {
            super.messageReceived( ctx, e );
        }

    }

    private void handleHttpRequest(ChannelHandlerContext channelContext, HttpRequest request) throws Exception {
        log.info( "handle HTTP: " + request  );
        if (isWebSocketsUpgradeRequest( request )) {
            log.info( "Processo websockets upgrade" );
            WebSocketContext context = this.contextRegistry.findContext( request.getHeader( "Host" ), request.getUri() );
            if (context != null) {

                Handshake handshake = findHandshake( request );

                if (handshake != null) {

                    HttpResponse response = handshake.generateResponse( context, request );
                    
                    log.info(  "content: " + response.getContent() );
                    response.addHeader( Names.UPGRADE, Values.WEBSOCKET );
                    response.addHeader( Names.CONNECTION, Values.UPGRADE );

                    ChannelPipeline pipeline = channelContext.getChannel().getPipeline();
                    reconfigureUpstream( pipeline );

                    addContextHandler( channelContext, context, pipeline );

                    channelContext.getChannel().write( response );

                    reconfigureDownstream( pipeline );

                    return;
                }
            }
        }

        // Send an error page otherwise.
        sendHttpResponse( channelContext, request, new DefaultHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN ) );
    }

    protected Handshake findHandshake(HttpRequest request) {
        for (Handshake handshake : this.handshakes) {
            log.info( "Test handshake: " + handshake );
            if (handshake.matches( request )) {
                return handshake;
            }
        }

        return null;
    }

    protected void reconfigureUpstream(ChannelPipeline pipeline) {
        log.info( "reconfiguring upstream pipeline" );
        pipeline.remove( "http-aggregator" );
        pipeline.replace( "http-decoder", "websockets-decoder", new WebSocketFrameDecoder() );
    }

    protected void reconfigureDownstream(ChannelPipeline pipeline) {
        log.info( "reconfiguring downstream pipeline" );
        pipeline.replace( "http-encoder", "websockets-encoder", new WebSocketFrameEncoder() );
    }

    protected void addContextHandler(ChannelHandlerContext channelContext, WebSocketContext context, ChannelPipeline pipeline) throws Exception {
        log.info( "attaching context handler to pipeline" );
        String sessionId = (String) channelContext.getAttachment();
        Session session = context.findSession( sessionId );
        WebSocketProcessorComponent component = context.createComponent( session );
        RubyWebSocketProcessorProxy proxy = new RubyWebSocketProcessorProxy( component );
        pipeline.addLast( "connection-handler", proxy );
    }

    protected boolean isWebSocketsUpgradeRequest(HttpRequest request) {
        return (Values.UPGRADE.equalsIgnoreCase( request.getHeader( Names.CONNECTION ) ) && Values.WEBSOCKET.equalsIgnoreCase( request.getHeader( Names.UPGRADE ) ));
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(
                    ChannelBuffers.copiedBuffer(
                            res.getStatus().toString(), CharsetUtil.UTF_8 ) );
            HttpHeaders.setContentLength( res, res.getContent().readableBytes() );
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write( res );
        if (!HttpHeaders.isKeepAlive( req ) || res.getStatus().getCode() != 200) {
            f.addListener( ChannelFutureListener.CLOSE );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets.protocol" );
    private ContextRegistry contextRegistry;
    private List<Handshake> handshakes = new ArrayList<Handshake>();

}
