package org.torquebox.test.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;

public class AbstractRubyTestCase {
	
	protected Ruby createRuby() {
		
		List<String> loadPaths = new ArrayList<String>();
		RubyInstanceConfig config = new RubyInstanceConfig();
		
		Map<String,String> env = new HashMap<String,String>();
		env.put( "GEM_PATH", "target/rubygems" );
		config.setEnvironment( env );
		
		Ruby ruby = JavaEmbedUtils.initialize(loadPaths, config );
		
		ruby.evalScriptlet( "require %q(rubygems)");
		return ruby;
	}

}
