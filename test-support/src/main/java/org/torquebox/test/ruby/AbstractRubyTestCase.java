package org.torquebox.test.ruby;

import java.util.ArrayList;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;

public class AbstractRubyTestCase {
	
	protected Ruby createRuby() {
		
		List<String> loadPaths = new ArrayList<String>();
		RubyInstanceConfig config = new RubyInstanceConfig();
		
		Ruby ruby = JavaEmbedUtils.initialize(loadPaths, config );
		
		ruby.evalScriptlet( "require %q(rubygems)");
		return ruby;
	}

}
