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
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.IOInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class implements the "rack.input" input stream, as defined in
 * http://rack.rubyforge.org/doc/SPEC.html
 */
public class RackChannel extends RubyObject {

    public static RubyClass createRackChannelClass(Ruby runtime) {
        RubyModule wunderBossModule = runtime.getOrCreateModule("WunderBoss");
        RubyClass rackChannel = wunderBossModule.getClass("RackChannel");
        if (rackChannel == null) {
            rackChannel = wunderBossModule.defineClassUnder("RackChannel",
                    runtime.getObject(), RACK_CHANNEL_ALLOCATOR);
            rackChannel.defineAnnotatedMethods(RackChannel.class);
        }
        return rackChannel;
    }

    private static final ObjectAllocator RACK_CHANNEL_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RackChannel(runtime, klass);
        }
    };

    private RackChannel(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
    }

    public RackChannel(Ruby runtime, RubyClass metaClass, InputStream inputStream) {
        super(runtime, metaClass);
        // Wrap the input stream in a RewindableChannel because Rack expects
        // 'rack.input' to be rewindable and a ServletInputStream is not
        // TODO: RewindableChannel isn't really needed now - we should incorporate its
        // rewinding directly into here
        inputChannel = new RewindableChannel(inputStream);
        // TODO: A more efficient implementation would create a single ByteBuffer
        // used by all read/gets/each methods instead of allocating a new one
        // per method call
    }

    @JRubyMethod
    public IRubyObject gets(ThreadContext context) {
        try {
            // TODO: 1 byte? really?
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            int bytesRead = inputChannel.read(byteBuffer);
            if (bytesRead == -1) { //EOF
                return getRuntime().getNil();
            } else {
                RubyString stringBuffer = RubyString.newEmptyString(getRuntime());
                byte readByte = byteBuffer.get(0);
                // 10 is newline
                while (readByte != 10) {
                    stringBuffer.cat(readByte);
                    byteBuffer.clear();
                    bytesRead = inputChannel.read(byteBuffer);
                    if (bytesRead == -1) {
                        break; // EOF
                    }
                    readByte = byteBuffer.get(0);
                }
                if (readByte == 10) {
                    stringBuffer.cat(readByte);
                }
                return stringBuffer;
            }
        } catch (IOException e) {
            throw getRuntime().newIOErrorFromException(e);
        }
    }

    @JRubyMethod(optional = 2)
    public IRubyObject read(ThreadContext context, IRubyObject[] args) throws IOException {
        long bytesToRead = Long.MAX_VALUE;
        boolean lengthGiven = false;
        if (args.length > 0 && !(args[0].isNil())) {
            bytesToRead = args[0].convertToInteger("to_i").getLongValue();
            lengthGiven = true;
        }
        RubyString stringBuffer = null;
        if (args.length > 1) {
            stringBuffer = args[1].convertToString();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate((int) Math.min(4*1024L, bytesToRead));
        int bytesRead = inputChannel.read(byteBuffer);
        long totalBytesRead = bytesRead;
        if (bytesRead == -1) { // EOF
            return lengthGiven ? getRuntime().getNil() : RubyString.newEmptyString(getRuntime());
        }

        byte[] bytes = byteBufferToBytes(byteBuffer, bytesRead);
        if (stringBuffer != null) {
            stringBuffer.clear();
            stringBuffer.cat(bytes);
        } else {
            stringBuffer = getRuntime().newString(new ByteList(bytes));
        }
        while (bytesRead != -1 && totalBytesRead < bytesToRead) {
            byteBuffer.clear();
            long bytesRemaining = bytesToRead - totalBytesRead;
            if (bytesRemaining < byteBuffer.limit()) {
                byteBuffer.limit((int) bytesRemaining);
            }
            bytesRead = inputChannel.read(byteBuffer);
            totalBytesRead += bytesRead;
            if (bytesRead > 0) {
                bytes = byteBufferToBytes(byteBuffer, bytesRead);
                stringBuffer.cat(bytes);
            }
        }
        return stringBuffer;
    }

    private byte[] byteBufferToBytes(ByteBuffer byteBuffer, int length) {
        byte[] bytes = new byte[length];
        byteBuffer.flip();
        byteBuffer.get(bytes);
        return bytes;
    }

    @JRubyMethod
    public IRubyObject each(ThreadContext context, Block block) {
        IRubyObject readLine = gets(context);
        while (readLine != getRuntime().getNil()) {
            block.yield(context, readLine);
            readLine = gets(context);
        }
        return getRuntime().getNil();
    }

    @JRubyMethod
    public IRubyObject rewind(ThreadContext context) throws IOException {
        inputChannel.position(0);
        return getRuntime().getNil();
    }

    @JRubyMethod(name="io=")
    public void setIO(IRubyObject rubyIO) {
        InputStream inputStream = new IOInputStream(rubyIO);
        inputChannel = new RewindableChannel(inputStream);
    }

    public void close() throws IOException {
        inputChannel.close();
    }

    private RewindableChannel inputChannel;
}
