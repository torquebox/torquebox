package org.torquebox.interp.core;

import org.jruby.RubyInstanceConfig;

public class CustomRubyInstanceConfig extends RubyInstanceConfig {
	
	private String jrubyHome;

	@Override
	public String getJRubyHome() {
		return this.jrubyHome;
	}

	@Override
	public void setJRubyHome(String jrubyHome) {
		this.jrubyHome = jrubyHome;
	}
	
	

}
