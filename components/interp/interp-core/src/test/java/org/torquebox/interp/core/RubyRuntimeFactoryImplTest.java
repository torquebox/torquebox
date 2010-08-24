package org.torquebox.interp.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.interp.spi.RuntimeInitializer;

public class RubyRuntimeFactoryImplTest {
	
	@Test
	public void testExplicitClassLoader() throws Exception {
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
		ClassLoader cl = new URLClassLoader(new URL[]{} );
		factory.setClassLoader( cl );
		assertSame( cl, factory.getClassLoader() );
	}
	
	@Test
	public void testContextClassLoader() throws Exception {
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
		ClassLoader cl = new URLClassLoader(new URL[]{} );
		Thread.currentThread().setContextClassLoader( cl );
		assertSame( cl, factory.getClassLoader() );
	}
	
	@Test
	public void testNullInitializerIsAllowed() throws Exception {
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
		factory.setUseJRubyHomeEnvVar( false );
		Ruby ruby = factory.create();
		assertNotNull( ruby );
	}
	
	@Test
	public void testInitializerIsUsed() throws Exception {
		MockRuntimeInitializer initializer = new MockRuntimeInitializer();
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
		factory.setUseJRubyHomeEnvVar( false );
		
		Ruby ruby = factory.create();
		
		assertNotNull( ruby );
		assertSame( ruby, initializer.ruby );
	}
	
	@Test
	public void testOpenSSL_HMAC_digest() throws Exception {
		MockRuntimeInitializer initializer = new MockRuntimeInitializer();
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
		factory.setUseJRubyHomeEnvVar( false );
		
		Ruby ruby = factory.create();
		
		
		String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
		Object result = ruby.evalScriptlet(script);
		
		assertNotNull( result );
	}
	
	@Test
	public void testApplicationEnvironment() throws Exception {
		MockRuntimeInitializer initializer = new MockRuntimeInitializer();
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
		Map<String,String> env = new HashMap<String,String>();
		env.put( "CHEESE", "taco" );
		factory.setApplicationEnvironment( env );
		Map<String,String> env2 = factory.createEnvironment();
		assertNotNull( env2.get( "CHEESE" ) );
		assertEquals( env.get( "CHEESE"), env2.get("CHEESE" ) );
	}
	
	static class MockRuntimeInitializer implements RuntimeInitializer {

		public Ruby ruby;

		@Override
		public void initialize(Ruby ruby) throws Exception {
			this.ruby = ruby;
		}
		
	}

}
