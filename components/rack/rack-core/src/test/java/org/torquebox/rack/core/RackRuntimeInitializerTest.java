package org.torquebox.rack.core;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

import static org.junit.Assert.*;

public class RackRuntimeInitializerTest extends AbstractRubyTestCase {

	@Test
	public void testInitializer() throws Exception {
		RackRuntimeInitializer initializer = new RackRuntimeInitializer(VFS.getChild( "/myapp" ), "test" );
		
		Ruby ruby = createRuby();
		initializer.initialize( ruby );
		
		String rackRoot = (String) ruby.evalScriptlet( "RACK_ROOT" ).toJava(String.class);
		assertEquals( "vfs:/myapp", rackRoot );
		
		String rackEnv = (String) ruby.evalScriptlet( "RACK_ENV" ).toJava(String.class);
		assertEquals( "test", rackEnv );

        String pwd = (String) ruby.evalScriptlet( "Dir.pwd" ).toJava(String.class);
        assertEquals( "/myapp", pwd );
	}
}
