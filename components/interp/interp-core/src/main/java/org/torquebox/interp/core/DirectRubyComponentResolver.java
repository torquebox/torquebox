package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class DirectRubyComponentResolver implements RubyComponentResolver {

	private IRubyObject component;

	public DirectRubyComponentResolver(IRubyObject component) {
		this.component = component;
	}
	
	@Override
	public IRubyObject resolve(Ruby ruby) {
		return this.component;
	}

}
