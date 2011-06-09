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

package org.torquebox.web.websockets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.catalina.Session;
import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;
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
        log.info( "handleHttpRequest - " + request );
        // Allow only GET methods.
        if (request.getMethod() != HttpMethod.GET) {
            sendHttpResponse( channelContext, request, new DefaultHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN ) );
            return;
        }

        if (isWebSocketsUpgradeRequest( request )) {
            log.info( "is upgrade to WebSockets" );
            WebSocketContext context = this.contextRegistry.findContext( request.getHeader( "Host" ), request.getUri() );

            // Serve the WebSocket handshake request.
            if (context != null) {
                // Create the WebSocket handshake response.
                HttpResponse response = new DefaultHttpResponse( HttpVersion.HTTP_1_1, new HttpResponseStatus( 101, "Web Socket Protocol Handshake" ) );
                response.addHeader( Names.UPGRADE, Values.WEBSOCKET );
                response.addHeader( Names.CONNECTION, Values.UPGRADE );

                // Fill in the headers and contents depending on handshake
                // method.
                if (request.containsHeader( Names.SEC_WEBSOCKET_KEY1 ) && request.containsHeader( Names.SEC_WEBSOCKET_KEY2 )) {
                    handleNewHandshake( context, request, response );
                } else {
                    handleOldHandshake( context, request, response );
                }

                // Upgrade the connection and send the handshake response.
                ChannelPipeline pipeline = channelContext.getChannel().getPipeline();
                reconfigureUpstream( pipeline );
                addContextHandler( channelContext, context, pipeline );
                channelContext.getChannel().write( response );
                reconfigureDownstream( pipeline );
                return;
            }
        }

        // Send an error page otherwise.
        sendHttpResponse( channelContext, request, new DefaultHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN ) );
    }

    protected void reconfigureUpstream(ChannelPipeline pipeline) {
        pipeline.remove( "http-aggregator" );
        pipeline.replace( "http-decoder", "websockets-decoder", new WebSocketFrameDecoder() );
    }

    protected void reconfigureDownstream(ChannelPipeline pipeline) {
        pipeline.replace( "http-encoder", "websockets-encoder", new WebSocketFrameEncoder() );
    }

    protected void addContextHandler(ChannelHandlerContext channelContext, WebSocketContext context, ChannelPipeline pipeline) throws Exception {
        String sessionId = (String) channelContext.getAttachment();
        Session session = context.findSession( sessionId );
        WebSocketProcessorComponent component = context.createComponent( session );
        pipeline.addLast( "connection-handler", new RubyWebSocketProcessorProxy( component ) );
        UpstreamChannelStateEvent connectEvent = new UpstreamChannelStateEvent( channelContext.getChannel(), ChannelState.CONNECTED, channelContext.getChannel().getRemoteAddress() );
        channelContext .sendUpstream( connectEvent );
    }

    protected boolean isWebSocketsUpgradeRequest(HttpRequest request) {
        return (Values.UPGRADE.equalsIgnoreCase( request.getHeader( Names.CONNECTION ) ) && Values.WEBSOCKET.equalsIgnoreCase( request.getHeader( Names.UPGRADE ) ));
    }

    protected void handleNewHandshake(WebSocketContext context, HttpRequest request, HttpResponse response) throws NoSuchAlgorithmException {

        response.addHeader( Names.SEC_WEBSOCKET_ORIGIN, request.getHeader( Names.ORIGIN ) );
        response.addHeader( Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation( context, request ) );
        String protocol = request.getHeader( Names.SEC_WEBSOCKET_PROTOCOL );

        if (protocol != null) {
            response.addHeader( Names.SEC_WEBSOCKET_PROTOCOL, protocol );
        }

        // Calculate the answer of the challenge.
        String key1 = request.getHeader( Names.SEC_WEBSOCKET_KEY1 );
        String key2 = request.getHeader( Names.SEC_WEBSOCKET_KEY2 );
        int a = (int) (Long.parseLong( key1.replaceAll( "[^0-9]", "" ) ) / key1.replaceAll( "[^ ]", "" ).length());
        int b = (int) (Long.parseLong( key2.replaceAll( "[^0-9]", "" ) ) / key2.replaceAll( "[^ ]", "" ).length());
        long c = request.getContent().readLong();

        ChannelBuffer input = ChannelBuffers.buffer( 16 );
        input.writeInt( a );
        input.writeInt( b );
        input.writeLong( c );

        ChannelBuffer output = ChannelBuffers.wrappedBuffer( MessageDigest.getInstance( "MD5" ).digest( input.array() ) );
        response.setContent( output );
    }

    protected void handleOldHandshake(WebSocketContext context, HttpRequest request, HttpResponse response) {
        // Old handshake method with no challenge:
        response.addHeader( Names.WEBSOCKET_ORIGIN, request.getHeader( Names.ORIGIN ) );
        response.addHeader( Names.WEBSOCKET_LOCATION, getWebSocketLocation( context, request ) );
        String protocol = response.getHeader( Names.WEBSOCKET_PROTOCOL );
        if (protocol != null) {
            response.addHeader( Names.WEBSOCKET_PROTOCOL, protocol );
        }
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

    private String getWebSocketLocation(WebSocketContext context, HttpRequest req) {
        return "ws://" + req.getHeader( HttpHeaders.Names.HOST ) + context.getContextPath();
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets.protocol" );
    private ContextRegistry contextRegistry;

}
