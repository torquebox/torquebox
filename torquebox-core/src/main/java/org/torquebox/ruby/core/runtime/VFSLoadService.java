package org.torquebox.ruby.core.runtime;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.runtime.load.LoadService;

public class VFSLoadService extends LoadService {
	
	private static final Logger log = Logger.getLogger(VFSLoadService.class );

	public VFSLoadService(Ruby runtime) {
		super(runtime);
	}

	@Override
	public void load(String name, boolean wrap) {
		log.info( "load(" + name + ", " + wrap + ")" );
		super.load(name, wrap);
	}

	@Override
	public boolean require(String file) {
		log.info( "require(" + file + ")" );
		return super.require(file);
	}
	
	

}
