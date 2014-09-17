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
import org.jruby.runtime.ObjectAllocator;
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

    public RackResponder(Ruby runtime, RubyClass metaClass, RackAdapter rackAdapter) {
        super(runtime, metaClass);
        this.rackAdapter = rackAdapter;
    }

    @JRubyMethod(name = "response_code=")
    public IRubyObject setResponseCode(final IRubyObject status) {
        rackAdapter.setResponseCode((Integer) status.toJava((Integer.class)));
        return getRuntime().getNil();
    }

    @JRubyMethod(name = "add_header")
    public IRubyObject addHeader(final RubyString rubyKey, final RubyString rubyValues) {
        // HTTP headers are always US_ASCII so we take a couple of shortcuts
        // for converting them from RubyStrings to Java Strings
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
                rackAdapter.addResponseHeader(rubyKey.getBytes(), value);
            } else if (i == charValues.length - 1) {
                String value = new String(charValues, offset, charValues.length - offset);
                rackAdapter.addResponseHeader(rubyKey.getBytes(), value);
            }
        }
        return getRuntime().getNil();
    }

    @JRubyMethod(name = "write")
    public IRubyObject write(final RubyString string) throws IOException {
        rackAdapter.write(string.getBytes());
        return getRuntime().getNil();
    }

    @JRubyMethod(name = "flush")
    public IRubyObject flush() throws IOException {
        rackAdapter.flush();
        return getRuntime().getNil();
    }

    private RackAdapter rackAdapter;
}
