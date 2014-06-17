/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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


package org.torquebox.web.rack;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class implements the "rack.input" input stream, as defined in
 * http://rack.rubyforge.org/doc/SPEC.html
 */
public class RackChannel extends RubyObject {

    public static RubyClass createRackChannelClass(Ruby runtime) {
        RubyModule torqueBoxModule = runtime.getOrCreateModule("TorqueBox");
        RubyClass rackChannel = torqueBoxModule.getClass("RackChannel");
        if (rackChannel == null) {
            rackChannel = torqueBoxModule.defineClassUnder("RackChannel",
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
        inputChannel = new RewindableChannel(inputStream);
    }

    @JRubyMethod
    public IRubyObject gets(ThreadContext context) {
        try {
            // 1 byte? really?
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
                     stringBuffer.cat(10);
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

    public void close() throws IOException {
        inputChannel.close();
    }

    private RewindableChannel inputChannel;
}

