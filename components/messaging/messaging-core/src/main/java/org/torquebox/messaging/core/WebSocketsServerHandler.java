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
package org.torquebox.messaging.core;

import java.security.MessageDigest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2314 $, $Date: 2010-06-22 09:02:27 +0200 (Mar, 22 jui 2010) $
 */
public class WebSocketsServerHandler extends SimpleChannelUpstreamHandler {

    private String path  = "/websockets";
    
    public WebSocketsServerHandler() {
    	super();
    }
    
    public WebSocketsServerHandler(String path) {
    	super();
    	this.path = path;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        // Allow only GET methods.
        if (req.getMethod() != HttpMethod.GET) {
            sendHttpResponse(
                    ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
            return;
        }

        // Serve the WebSocket handshake request.
        if (req.getUri().equals(path) &&
            Values.UPGRADE.equalsIgnoreCase(req.getHeader(Names.CONNECTION)) &&
            Values.WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE))) {
            // Create the WebSocket handshake response.
            HttpResponse res = new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1,
                    new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
            res.addHeader(Names.UPGRADE, Values.WEBSOCKET);
            res.addHeader(Names.CONNECTION, Values.UPGRADE);

            // Fill in the headers and contents depending on handshake method.
            if (req.containsHeader(Names.SEC_WEBSOCKET_KEY1) &&
                req.containsHeader(Names.SEC_WEBSOCKET_KEY2)) {
                // New handshake method with a challenge:
                res.addHeader(Names.SEC_WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN));
                res.addHeader(Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
                String protocol = req.getHeader(Names.SEC_WEBSOCKET_PROTOCOL);
                if (protocol != null) {
                    res.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
                }

                // Calculate the answer of the challenge.
                String key1 = req.getHeader(Names.SEC_WEBSOCKET_KEY1);
                String key2 = req.getHeader(Names.SEC_WEBSOCKET_KEY2);
                int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
                int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
                long c = req.getContent().readLong();
                ChannelBuffer input = ChannelBuffers.buffer(16);
                input.writeInt(a);
                input.writeInt(b);
                input.writeLong(c);
                ChannelBuffer output = ChannelBuffers.wrappedBuffer(
                        MessageDigest.getInstance("MD5").digest(input.array()));
                res.setContent(output);
            } else {
                // Old handshake method with no challenge:
                res.addHeader(Names.WEBSOCKET_ORIGIN, req.getHeader(Names.ORIGIN));
                res.addHeader(Names.WEBSOCKET_LOCATION, getWebSocketLocation(req));
                String protocol = req.getHeader(Names.WEBSOCKET_PROTOCOL);
                if (protocol != null) {
                    res.addHeader(Names.WEBSOCKET_PROTOCOL, protocol);
                }
            }

            // Upgrade the connection and send the handshake response.
            ChannelPipeline p = ctx.getChannel().getPipeline();
            p.remove("aggregator");
            p.replace("decoder", "ws-decoder", new WebSocketFrameDecoder());

            ctx.getChannel().write(res);
            p.replace("encoder", "ws-encoder", new WebSocketFrameEncoder());
            return;
        }

        // Send an error page otherwise.
        sendHttpResponse(
                ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    	Channels.fireMessageReceived(ctx, frame);
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(
                    ChannelBuffers.copiedBuffer(
                            res.getStatus().toString(), CharsetUtil.UTF_8));
            HttpHeaders.setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + path;
    }
}
