package org.torquebox.rack.spi;

import org.jruby.RubyHash;

public interface RackEnvironment {

    RubyHash getEnv();

}
