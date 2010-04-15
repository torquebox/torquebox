package org.torquebox.interp.core;

import java.net.URL;
import java.net.URLClassLoader;

import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.interp.spi.RuntimeInitializer;

import static org.junit.Assert.*;

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
		Ruby ruby = factory.create();
		assertNotNull( ruby );
	}
	
	@Test
	public void testInitializerIsUsed() throws Exception {
		MockRuntimeInitializer initializer = new MockRuntimeInitializer();
		RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
		
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
