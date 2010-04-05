package org.torquebox.test.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.core.DefaultRubyRuntimeFactory;
import org.torquebox.interp.core.VFSLoadServiceCreator;

import static org.junit.Assert.*;

public class AbstractRubyTestCase {
	
	protected Ruby createRuby() throws Exception {
		
		/*
		List<String> loadPaths = new ArrayList<String>();
		RubyInstanceConfig config = new RubyInstanceConfig();
		
		config.setLoadServiceCreator(new VFSLoadServiceCreator());
		*/
		
		DefaultRubyRuntimeFactory factory = new DefaultRubyRuntimeFactory();
		factory.setGemPath( "target/rubygems" );
		
		Ruby ruby = factory.create();
		
		ruby.evalScriptlet( "require %q(rubygems)");
		return ruby;
	}
	
	protected void assertNotNil(IRubyObject obj) {
		assertFalse( "object is a Ruby nil", obj.isNil() );
	}

}
