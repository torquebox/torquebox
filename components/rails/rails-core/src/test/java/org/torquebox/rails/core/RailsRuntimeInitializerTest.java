package org.torquebox.rails.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RailsRuntimeInitializerTest extends AbstractRubyTestCase {

	@Test
	public void testInitializeWithGems() throws Exception {
		Class.forName(VFS.class.getName());
		Ruby ruby = createRuby();

		String railsRootStr = System.getProperty("user.dir") + "/src/test/rails/ballast";
		String vfsRailsRootStr = "vfs:" + railsRootStr;
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		System.err.println("railsRoot=" + railsRoot);

		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer(railsRoot, "development", true);

		initializer.initialize(ruby);

		RubyClass objectClass = (RubyClass) ruby.getClassFromPath("Object");

		IRubyObject rubyRailsRoot = objectClass.getConstant("RAILS_ROOT");
		assertNotNull(rubyRailsRoot);
		assertNotNil(rubyRailsRoot);
		assertEquals(vfsRailsRootStr, rubyRailsRoot.toString());

		IRubyObject rubyRailsEnv = objectClass.getConstant("RAILS_ENV");
		assertNotNull(rubyRailsEnv);
		assertNotNil(rubyRailsEnv);
		assertEquals("development", rubyRailsEnv.toString());
		ruby.tearDown(false);
	}

	@Test(expected = RaiseException.class)
	public void testUnknownModelsAreCorrectlyIdentified() throws Exception {
		Class.forName(VFS.class.getName());
		Ruby ruby = createRuby();

		String railsRootStr = System.getProperty("user.dir") + "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer(railsRoot, "development", true);

		initializer.initialize(ruby);

		String script = "class Foo; def self.fetch(); NoSuchModel; end; end; Foo.fetch";
		Object noSuchModel = ruby.evalScriptlet(script);
		ruby.tearDown(false);
	}

	@Test
	public void testModelsAreLoadable() throws Exception {
		Class.forName(VFS.class.getName());
		Ruby ruby = createRuby();

		String railsRootStr = System.getProperty("user.dir") + "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer(railsRoot, "development", true);

		initializer.initialize(ruby);

		RubyModule bookModel = ruby.getClassFromPath("Book");
		assertNotNil(bookModel);
		ruby.tearDown(false);

	}
	@Test
	public void testOpenSSL_HMAC_digest() throws Exception {
		Class.forName(VFS.class.getName());
		Ruby ruby = createRuby();
		
		String railsRootStr = System.getProperty("user.dir") + "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);
		RailsRuntimeInitializer initializer = new RailsRuntimeInitializer(railsRoot, "development", true);
		
		initializer.initialize(ruby);
		
		String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
		Object result = ruby.evalScriptlet(script);
		
		System.err.println( "result=" + result );
		ruby.tearDown(false);
		
	}
}
