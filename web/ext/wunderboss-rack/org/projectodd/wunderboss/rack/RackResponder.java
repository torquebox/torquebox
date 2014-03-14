package org.projectodd.wunderboss.rack;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.io.IOException;

public class RackResponder extends RubyObject {

    public static RubyClass createRackResponderClass(Ruby runtime) {
        RubyModule wunderBossModule = runtime.getOrCreateModule("WunderBoss");
        RubyClass rubyClass = wunderBossModule.getClass("RackResponder");
        if (rubyClass == null) {
            rubyClass = wunderBossModule.defineClassUnder("RackResponder",
                    runtime.getObject(), RACK_RESPONDER_ALLOCATOR);
            rubyClass.defineAnnotatedMethods(RackResponder.class);
        }
        return rubyClass;
    }

    private static final ObjectAllocator RACK_RESPONDER_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RackResponder(runtime, klass);
        }
    };

    private RackResponder(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
    }

    public RackResponder(Ruby runtime, RubyClass metaClass, HttpServerExchange exchange) {
        super(runtime, metaClass);
        this.exchange = exchange;
    }

    @JRubyMethod(name = "response_code=")
    public IRubyObject setResponseCode(final IRubyObject status) {
        exchange.setResponseCode((Integer) status.toJava((Integer.class)));
        return getRuntime().getNil();
    }

    @JRubyMethod(name = "add_header")
    public IRubyObject addHeader(final RubyString rubyKey, final RubyString rubyValues) {
        // HTTP headers are always US_ASCII so we take a couple of shortcuts
        // for converting them from RubyStrings to Java Strings
        final HttpString key = new HttpString(rubyKey.getBytes());
        final byte[] byteValues = rubyValues.getBytes();
        final char[] charValues = new char[byteValues.length];
        int i;
        int offset = 0;
        // split header values on newlines while converting from bytes to chars
        for (i = 0; i < charValues.length; i++) {
            charValues[i] = (char) byteValues[i];
            if (charValues[i] == '\n') {
                String value = new String(charValues, offset, i - offset);
                offset = i + 1;
                addHeader(key, value);
            } else if (i == charValues.length - 1) {
                String value = new String(charValues, offset, charValues.length - offset);
                addHeader(key, value);
            }
        }
        return getRuntime().getNil();
    }

    private void addHeader(final HttpString key, final String value) {
        // Leave out the transfer-encoding header since the container takes
        // care of chunking responses and adding that header
        if (!Headers.TRANSFER_ENCODING.equals(key) && !"chunked".equals(value)) {
            exchange.getResponseHeaders().add(key, value);
        }
    }

    @JRubyMethod(name = "write")
    public IRubyObject write(final RubyString string) throws IOException {
        exchange.getOutputStream().write(string.getBytes());
        return getRuntime().getNil();
    }

    @JRubyMethod(name = "flush")
    public IRubyObject flush() throws IOException {
        exchange.getOutputStream().flush();
        return getRuntime().getNil();
    }

    private HttpServerExchange exchange;
}
