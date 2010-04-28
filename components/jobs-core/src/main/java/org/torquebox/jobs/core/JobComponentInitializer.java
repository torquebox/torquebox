package org.torquebox.jobs.core;

import org.jboss.logging.Logger;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.interp.spi.ComponentInitializer;

public class JobComponentInitializer implements ComponentInitializer {

	@Override
	public void initialize(IRubyObject object) throws Exception {
		String rubyClassName = object.getMetaClass().getName();
		String loggerName = rubyClassName.replaceAll("::", ".");
		Logger log = Logger.getLogger( loggerName );
		ReflectionHelper.setIfPossible(object.getRuntime(), object, "log", log);
	}

}
