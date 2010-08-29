package org.torquebox.rack.spi;

import java.util.Map;

import org.jruby.RubyHash;

public interface RackEnvironment {
	
	Map<String, Object> getEnv();

}
