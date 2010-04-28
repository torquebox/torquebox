package org.torquebox.interp.spi;

import org.jruby.runtime.builtin.IRubyObject;

public interface ComponentInitializer {
	
	void initialize(IRubyObject object) throws Exception;

}
