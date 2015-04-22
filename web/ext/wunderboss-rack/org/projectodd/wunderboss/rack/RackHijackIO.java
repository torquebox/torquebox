package org.projectodd.wunderboss.rack;

import org.jruby.Ruby;
import org.jruby.RubyIO;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class RackHijackIO extends RubyIO {

    public RackHijackIO(Ruby runtime, ReadableByteChannel inputChannel, WritableByteChannel outputChannel) {
        super(runtime, new RackHijackChannel(inputChannel, outputChannel));
        super.getMetaClass().defineAnnotatedMethods(RackHijackIO.class);
    }

    // Purely to appease TubeSock, which assumes a peeraddr method exists
    @JRubyMethod
    public IRubyObject peeraddr(ThreadContext context) {
        Ruby runtime = context.runtime;
        int port = 0;
        String hostName = "0.0.0.0";
        String hostAddress = "0.0.0.0";
        return runtime.newArray(
                runtime.newString("AF_INET"),
                runtime.newFixnum(port),
                runtime.newString(hostName),
                runtime.newString(hostAddress)
        );
    }

    private static class RackHijackChannel implements ReadableByteChannel, WritableByteChannel {
        public RackHijackChannel(ReadableByteChannel inputChannel, WritableByteChannel outputChannel) {
            this.inputChannel = inputChannel;
            this.outputChannel = outputChannel;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int read = inputChannel.read(dst);
            // JRuby expects us to do blocking reads here, so block
            while (read == 0) {
                read = inputChannel.read(dst);
            }
            return read;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return outputChannel.write(src);
        }

        @Override
        public boolean isOpen() {
            return inputChannel.isOpen() || outputChannel.isOpen();
        }

        @Override
        public void close() throws IOException {
            try {
                inputChannel.close();
            } finally {
                outputChannel.close();
            }
        }

        private ReadableByteChannel inputChannel;
        private WritableByteChannel outputChannel;
    }
}
