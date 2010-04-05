package org.torquebox.rails.core;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

import static org.junit.Assert.*;

public class RailsRuntimeInitializerTest extends AbstractRubyTestCase {

	@Test
	public void testInitializeWithGems() throws Exception {
		Ruby ruby = createRuby();
		
		String railsRootStr = System.getProperty( "user.dir" ) + "/src/test/rails/ballast-2.3.5";
		String vfsRailsRootStr = "vfs://" + railsRootStr;
		VirtualFile railsRoot = VFS.getChild( railsRootStr );
		
		System.err.println( "railsRoot=" + railsRoot);
		
		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer( railsRoot, "development", true );
		
		initializer.initialize( ruby );
		
		RubyClass objectClass = (RubyClass) ruby.getClassFromPath( "Object" );
		
		IRubyObject rubyRailsRoot =  objectClass.getConstant("RAILS_ROOT");
		assertNotNull( rubyRailsRoot );
		assertNotNil( rubyRailsRoot );
		assertEquals( vfsRailsRootStr, rubyRailsRoot.toString() );
		
		IRubyObject rubyRailsEnv = objectClass.getConstant( "RAILS_ENV" );
		assertNotNull( rubyRailsEnv );
		assertNotNil( rubyRailsEnv );
		assertEquals( "development", rubyRailsEnv.toString() );
	}
}
