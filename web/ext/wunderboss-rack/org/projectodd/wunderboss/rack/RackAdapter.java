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

import org.jruby.RubyHash;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface RackAdapter {

    public String getPathInfo();

    public String getScriptName();

    public String getRequestMethod();

    public String getQueryString();

    public String getScheme();

    public String getHostName();

    public int getPort();

    public String getRemoteAddr();

    public String getContentType();

    public int getContentLength();

    public InputStream getInputStream();

    public ReadableByteChannel getInputChannel();

    public WritableByteChannel getOutputChannel();

    public void populateRackHeaders(RubyHash rackEnv);

    public void populateRackHeaderFromBytes(RubyHash rackEnv, byte[] httpBytes, int from ,int to, byte[] rubyBytes);

    public void setResponseCode(int responseCode);

    public void addResponseHeader(byte[] keyBytes, int keyOffset, int keyLength, String value);

    public void write(byte[] bytes, int offset, int length) throws IOException;

    public void flush() throws IOException;

    public void async();
}
