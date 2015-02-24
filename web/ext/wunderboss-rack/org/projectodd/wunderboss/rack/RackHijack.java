package org.projectodd.wunderboss.rack;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyIO;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class RackHijack extends RubyObject {

    public static RubyClass createRackHijackClass(Ruby runtime) {
        RubyModule wunderBossModule = runtime.getOrCreateModule("WunderBoss");
        RubyClass rackHijack = wunderBossModule.getClass("RackHijack");
        if (rackHijack == null) {
            rackHijack = wunderBossModule.defineClassUnder("RackHijack",
                    runtime.getObject(), RACK_HIJACK_ALLOCATOR);
            rackHijack.defineAnnotatedMethods(RackHijack.class);
        }
        return rackHijack;
    }

    private static final ObjectAllocator RACK_HIJACK_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RackHijack(runtime, klass);
        }
    };

    private RackHijack(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
    }

    public RackHijack(Ruby runtime, RubyClass metaClass, RackAdapter rackAdapter, RackEnvironmentHash rackEnvHash) {
        super(runtime, metaClass);
        this.rackAdapter = rackAdapter;
        this.rackEnvHash = rackEnvHash;
    }

    @JRubyMethod
    public IRubyObject call() {
        rackAdapter.async();
        IRubyObject rackHijackIO = new RackHijackIO(getRuntime(), rackAdapter.getInputChannel(),
                rackAdapter.getOutputChannel());
        rackEnvHash.lazyPut(RackEnvironment.RACK_KEY.RACK_HIJACK_IO, rackHijackIO, false);
        return rackHijackIO;
    }

    private RackAdapter rackAdapter;
    private RackEnvironmentHash rackEnvHash;
}
