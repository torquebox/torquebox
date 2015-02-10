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

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.projectodd.wunderboss.ruby.RubyHelper;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;

public class ServletRackAdapter implements RackAdapter {

    public ServletRackAdapter(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getPathInfo() {
        String pathInfo = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        // strip contextPath and servletPath from pathInfo
        if (pathInfo.startsWith(contextPath) && !contextPath.equals("/")) {
            pathInfo = pathInfo.substring(contextPath.length());
        }
        if (pathInfo.startsWith(servletPath) && !servletPath.equals("/")) {
            pathInfo = pathInfo.substring(servletPath.length());
        }
        return pathInfo;
    }

    @Override
    public String getScriptName() {
        return request.getContextPath() + request.getServletPath();
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod();
    }

    @Override
    public String getQueryString() {
        String queryString = request.getQueryString();
        return queryString == null ? "" : queryString;
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getHostName() {
        return request.getServerName();
    }

    @Override
    public int getPort() {
        return request.getServerPort();
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getContentType() {
        return request.getContentType() + "";
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return request.getInputStream();
        } catch (IOException ex) {
            throw new RuntimeException("Error getting servlet input stream", ex);
        }
    }

    @Override
    public ReadableByteChannel getInputChannel() {
        return Channels.newChannel(getInputStream());
    }

    @Override
    public WritableByteChannel getOutputChannel() {
        try {
            return Channels.newChannel(response.getOutputStream());
        } catch (IOException ex) {
            throw new RuntimeException("Error getting servlet output stream", ex);
        }
    }

    @Override
    public void populateRackHeaders(final RubyHash rackEnv) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            fillHeaderKey(rackEnv, headerName, rackHeaderNameToBytes(headerName));
        }
    }

    @Override
    public void populateRackHeaderFromBytes(final RubyHash rackEnv, byte[] httpBytes, int from, int to, byte[] rubyBytes) {
        String headerName = new String(httpBytes, from, to - from);
        fillHeaderKey(rackEnv, headerName, rubyBytes);
    }

    @Override
    public void setResponseCode(int responseCode) {
        response.setStatus(responseCode);
    }

    @Override
    public void addResponseHeader(byte[] keyBytes, int keyOffset, int keyLength, String value) {
        String key = new String(keyBytes, keyOffset, keyLength);
        if (!"transfer-encoding".equals(key.toLowerCase()) && !"chunked".equals(value)) {
            response.addHeader(key, value);
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        response.getOutputStream().write(bytes, offset, length);
    }

    @Override
    public void flush() throws IOException {
        response.flushBuffer();
    }

    @Override
    public void async() {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(0); // no timeout
    }

    private void fillHeaderKey(final RubyHash rackEnv, final String key, byte[] rubyKeyBytes) {
        String lowercasedKey = key.toLowerCase();
        // RACK spec says not to create HTTP_CONTENT_TYPE or HTTP_CONTENT_LENGTH headers
        if (!lowercasedKey.equals("content-type") && !lowercasedKey.equals("content-length")) {
            Enumeration<String> headerValues = request.getHeaders(key);
            if (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                while (headerValues.hasMoreElements()) {
                    headerValue += "\n" + headerValues.nextElement();
                }
                Ruby runtime = rackEnv.getRuntime();
                RubyString rubyKey = RubyHelper.toUsAsciiRubyString(runtime, rubyKeyBytes);
                rackEnv.put(rubyKey, RubyHelper.toUnicodeRubyString(runtime, headerValue));
            }
        }
    }

    private static byte[] rackHeaderNameToBytes(final String headerName) {
        // This is a more performant implemention of:
        // "HTTP_" + headerName.toUpperCase().replace('-', '_');
        byte[] envNameBytes = new byte[headerName.length() + 5];
        envNameBytes[0] = 'H';
        envNameBytes[1] = 'T';
        envNameBytes[2] = 'T';
        envNameBytes[3] = 'P';
        envNameBytes[4] = '_';
        for (int i = 5; i < envNameBytes.length; i++) {
            envNameBytes[i] = (byte) RackHelper.rackHeaderize(headerName.charAt(i - 5));
        }
        return envNameBytes;
    }

    private final HttpServletRequest request;
    private final HttpServletResponse response;
}
