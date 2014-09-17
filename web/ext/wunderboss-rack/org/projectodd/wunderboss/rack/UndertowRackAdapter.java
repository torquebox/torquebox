/*
* Copyright 2014 Red Hat, Inc, and individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.projectodd.wunderboss.rack;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.projectodd.wunderboss.ruby.RubyHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UndertowRackAdapter implements RackAdapter {

    public UndertowRackAdapter(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getPathInfo() {
        // Don't use exchange.getRequestPath() because that gets decoded by Undertow
        String pathInfo = exchange.getRequestURI();
        String resolvedPath = exchange.getResolvedPath();

        // strip the equivalent of servletPath from pathInfo
        if (pathInfo.startsWith(resolvedPath) && !resolvedPath.equals("/")) {
            pathInfo = pathInfo.substring(resolvedPath.length());
        }

        return pathInfo;
    }

    @Override
    public String getScriptName() {
        return exchange.getResolvedPath();
    }

    @Override
    public String getRequestMethod() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public String getQueryString() {
        return exchange.getQueryString();
    }

    @Override
    public String getScheme() {
        return exchange.getRequestScheme();
    }

    @Override
    public String getHostName() {
        return exchange.getHostName();
    }

    @Override
    public int getPort() {
        return exchange.getDestinationAddress().getPort();
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress sourceAddress = exchange.getSourceAddress();
        if(sourceAddress == null) {
            return "";
        }
        InetAddress address = sourceAddress.getAddress();
        if(address == null) {
            return "";
        }
        return address.getHostAddress();
    }

    @Override
    public String getContentType() {
        return getHeaders().getFirst(Headers.CONTENT_TYPE) + "";
    }

    @Override
    public int getContentLength() {
        final String contentLengthStr = getHeaders().getFirst(Headers.CONTENT_LENGTH);
        if (contentLengthStr == null || contentLengthStr.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(contentLengthStr);
    }

    @Override
    public void populateRackHeaders(final RubyHash rackEnv) {
        for (HttpString headerName : getHeaders().getHeaderNames()) {
            fillHeaderKey(rackEnv, headerName, rackHeaderNameToBytes(headerName));
        }
    }

    @Override
    public void populateRackHeaderFromBytes(final RubyHash rackEnv, byte[] httpBytes, int from, int to, byte[] rubyBytes) {
        // this HttpString ctor has misleading variable names -
        // it's a copy from/to, not offset/length
        HttpString httpString = new HttpString(httpBytes, from, to);
        fillHeaderKey(rackEnv, httpString, rubyBytes);
    }

    @Override
    public void setResponseCode(int responseCode) {
        exchange.setResponseCode(responseCode);
    }

    @Override
    public void addResponseHeader(byte[] keyBytes, String value) {
        final HttpString key = new HttpString(keyBytes);
        // Leave out the transfer-encoding header since the container takes
        // care of chunking responses and adding that header
        if (!Headers.TRANSFER_ENCODING.equals(key) && !"chunked".equals(value)) {
            exchange.getResponseHeaders().add(key, value);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        exchange.getOutputStream().write(bytes);
    }

    @Override
    public void flush() throws IOException {
        exchange.getOutputStream().flush();
    }

    private void fillHeaderKey(final RubyHash rackEnv, final HttpString key, byte[] rubyKeyBytes) {
        // RACK spec says not to create HTTP_CONTENT_TYPE or HTTP_CONTENT_LENGTH headers
        if (!key.equals(Headers.CONTENT_TYPE) && !key.equals(Headers.CONTENT_LENGTH)) {
            HeaderValues headerValues = getHeaders().get(key);
            if (headerValues != null) {
                String headerValue = headerValues.get(0);
                int valueIndex = 1;
                while (valueIndex < headerValues.size()) {
                    headerValue += "\n" + headerValues.get(valueIndex++);
                }
                Ruby runtime = rackEnv.getRuntime();
                RubyString rubyKey = RubyHelper.toUsAsciiRubyString(runtime, rubyKeyBytes);
                rackEnv.put(rubyKey, RubyHelper.toUnicodeRubyString(runtime, headerValue));
            }
        }
    }

    private static byte[] rackHeaderNameToBytes(final HttpString headerName) {
        // This is a more performant implemention of:
        // "HTTP_" + headerName.toUpperCase().replace('-', '_');
        byte[] envNameBytes = new byte[headerName.length() + 5];
        envNameBytes[0] = 'H';
        envNameBytes[1] = 'T';
        envNameBytes[2] = 'T';
        envNameBytes[3] = 'P';
        envNameBytes[4] = '_';
        for (int i = 5; i < envNameBytes.length; i++) {
            envNameBytes[i] = (byte) RackHelper.rackHeaderize((char) headerName.byteAt(i - 5));
        }
        return envNameBytes;
    }

    private HeaderMap getHeaders() {
        return exchange.getRequestHeaders();
    }

    private final HttpServerExchange exchange;
}
