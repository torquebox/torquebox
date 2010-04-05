package org.torquebox.rails.core;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RailsRuntimeInitializerTest extends AbstractRubyTestCase {

	@Test
	public void testInitializeWithGems() throws Exception {
		Ruby ruby = createRuby();
		
		VirtualFile railsRoot = VFS.getChild( System.getProperty( "user.dir" ) + "/src/test/rails/ballast-2.3.5");
		
		System.err.println( "railsRoot=" + railsRoot);
		
		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer( railsRoot, "development", true );
		
		initializer.initialize( ruby );
	}
}
