/* Copyright 2010 Red Hat, Inc. */

package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimeFactory;

public class SingletonRubyRuntimeFactory implements RubyRuntimeFactory {

	private Ruby rubyRuntime;
	
	public SingletonRubyRuntimeFactory(Ruby rubyRuntime) {
		this.rubyRuntime = rubyRuntime;
	}
	
	public void setRubyRuntime(Ruby rubyRuntime) {
		this.rubyRuntime = rubyRuntime;
	}
	
	public Ruby getRubyRuntime() {
		return this.rubyRuntime;
	}
	
	@Override
	public Ruby create() throws Exception {
		return getRubyRuntime();
	}

}
