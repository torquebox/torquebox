package org.torquebox.test.ruby;

import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class AbstractRubyTestCase {
	
	protected Ruby createRuby() throws Exception {
		return TestRubyFactory.createRuby();
	}
	
	protected void assertNotNil(IRubyObject obj) {
		assertFalse( "object is a Ruby nil", obj.isNil() );
	}

}
