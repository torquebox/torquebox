package org.torquebox.interp.core;

import java.net.URL;
import java.net.URLClassLoader;

import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.interp.spi.RuntimeInitializer;

import static org.junit.Assert.*;

public class DefaultRubyRuntimeFactoryTest {
	
	@Test
	public void testExplicitClassLoader() throws Exception {
		DefaultRubyRuntimeFactory factory = new DefaultRubyRuntimeFactory();
		ClassLoader cl = new URLClassLoader(new URL[]{} );
		factory.setClassLoader( cl );
		assertSame( cl, factory.getClassLoader() );
	}
	
	@Test
	public void testContextClassLoader() throws Exception {
		DefaultRubyRuntimeFactory factory = new DefaultRubyRuntimeFactory();
		ClassLoader cl = new URLClassLoader(new URL[]{} );
		Thread.currentThread().setContextClassLoader( cl );
		assertSame( cl, factory.getClassLoader() );
	}
	
	@Test
	public void testNullInitializerIsAllowed() throws Exception {
		DefaultRubyRuntimeFactory factory = new DefaultRubyRuntimeFactory( null );
		Ruby ruby = factory.create();
		assertNotNull( ruby );
	}
	
	@Test
	public void testInitializerIsUsed() throws Exception {
		MockRuntimeInitializer initializer = new MockRuntimeInitializer();
		DefaultRubyRuntimeFactory factory = new DefaultRubyRuntimeFactory( initializer );
		
		Ruby ruby = factory.create();
		
		assertNotNull( ruby );
		assertSame( ruby, initializer.ruby );
	}
	
	static class MockRuntimeInitializer implements RuntimeInitializer {

		public Ruby ruby;

		@Override
		public void initialize(Ruby ruby) throws Exception {
			this.ruby = ruby;
		}
		
	}

}
