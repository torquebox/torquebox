package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.LoadServiceCreator;
import org.jruby.runtime.load.LoadService;

public class VFSLoadServiceCreator implements LoadServiceCreator {

	@Override
	public LoadService create(Ruby ruby) {
		return new VFSLoadService( ruby );
	}

}
