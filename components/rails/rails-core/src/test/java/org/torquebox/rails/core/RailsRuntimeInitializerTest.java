package org.torquebox.rails.core;

import static org.junit.Assert.*;
import java.util.*;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.metadata.RailsApplicationMetaData;


public class RailsRuntimeInitializerTest extends AbstractRubyTestCase {
	
	private Ruby ruby;

	@Before
	public void setUp() throws Exception {
		Class.forName(VFS.class.getName());
		ruby = createRuby();
	}
	
	@After
	public void tearDown() throws Exception {
		ruby.tearDown( false );
		ruby = null;
		System.gc();
	}

	@Test
	public void testInitializeWithGems() throws Exception {
		String railsRootStr = System.getProperty("user.dir")
				+ "/src/test/rails/ballast";
		String vfsRailsRootStr = "vfs:" + railsRootStr;
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		System.err.println("railsRoot=" + railsRoot);

		RailsRuntimeInitializer initializer = create( railsRoot, "development" );

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
	}

	@Test(expected = RaiseException.class)
	public void testUnknownModelsAreCorrectlyIdentified() throws Exception {
		String railsRootStr = System.getProperty("user.dir")
				+ "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		RailsRuntimeInitializer initializer = create( railsRoot, "development" );

		initializer.initialize(ruby);

		String script = "class Foo; def self.fetch(); NoSuchModel; end; end; Foo.fetch";
		Object noSuchModel = ruby.evalScriptlet(script);
	}

	@Test
	public void testModelsAreLoadable() throws Exception {
		String railsRootStr = System.getProperty("user.dir")
				+ "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);

		RailsRuntimeInitializer initializer = create( railsRoot, "development" );

		initializer.initialize(ruby);

		RubyModule bookModel = ruby.getClassFromPath("Book");
		assertNotNil(bookModel);
	}

	@Test
	public void testOpenSSL_HMAC_digest() throws Exception {
		String railsRootStr = System.getProperty("user.dir")
				+ "/src/test/rails/ballast";
		VirtualFile railsRoot = VFS.getChild(railsRootStr);
		RailsRuntimeInitializer initializer = create( railsRoot, "development" );

		initializer.initialize(ruby);

		String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
		Object result = ruby.evalScriptlet(script);

		System.err.println("result=" + result);
	}

    @Test
    public void testAutoloadPathsAvailableAsRubyConstant() throws Exception {
        String path = System.getProperty("user.dir") + "/src/test/rails/ballast";
        VirtualFile root = VFS.getChild(path);

        RailsRuntimeInitializer initializer = create( root, "development" );
        initializer.addAutoloadPath("path1");
        initializer.addAutoloadPath("path2");

        initializer.initialize(ruby);

        RubyModule object = ruby.getClassFromPath("Object");

        IRubyObject autoloadPaths = object.getConstant("TORQUEBOX_RAILS_AUTOLOAD_PATHS");
        assertNotNull(autoloadPaths);

        List<String> paths = (List<String>) JavaEmbedUtils.rubyToJava(autoloadPaths);
        assertTrue(paths.size()==2);
        assertTrue(paths.contains("path1"));
        assertTrue(paths.contains("path2"));
        
        String load_paths = "" + ruby.evalScriptlet("ActiveSupport::Dependencies.load_paths.join(',')");
        assertTrue(load_paths.endsWith(",path1,path2"));
    }

    private RailsRuntimeInitializer create(VirtualFile root, String env) {
        RackApplicationMetaData rackMetaData = new RackApplicationMetaData();
        rackMetaData.setRackRoot(root);
        rackMetaData.setRackEnv(env);
        RailsApplicationMetaData railsMetaData = new RailsApplicationMetaData( rackMetaData );
        return new RailsRuntimeInitializer( railsMetaData );
    }
}
