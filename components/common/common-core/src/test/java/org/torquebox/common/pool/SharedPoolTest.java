package org.torquebox.common.pool;

import org.junit.Test;
import org.torquebox.common.spi.InstanceFactory;

import static org.junit.Assert.*;


public class SharedPoolTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testNoInstanceOrFactory() throws Exception {
		SharedPool<String> pool = new SharedPool<String>();
		
		pool.create();
	}
	
	@Test
	public void testInitialInstanceViaCtor() throws Exception {
		String instance = "tacos";
		
		SharedPool<String> pool = new SharedPool<String>( instance );
		
		pool.create();
		
		for ( int i = 0 ; i < 100 ; ++i ) {
			assertEquals( instance, pool.borrowInstance() );
		}
	}
	
	@Test
	public void testInitialInstanceViaAccessor() throws Exception {
		String instance = "tacos";
		
		SharedPool<String> pool = new SharedPool<String>();
		pool.setInstance( instance );
		
		pool.create();
		
		for ( int i = 0 ; i < 100 ; ++i ) {
			assertEquals( instance, pool.borrowInstance() );
		}		
	}
	
	@Test
	public void testInstanceFactoryViaCtor() throws Exception {
		String instance = "tacos";
		
		SharedPool<String> pool = new SharedPool<String>( new MockInstanceFactory( instance ) );
		
		pool.create();
		
		for ( int i = 0 ; i < 100 ; ++i ) {
			assertEquals( instance, pool.borrowInstance() );
		}		
	}
	
	@Test
	public void testInstanceFactoryViaAccessor() throws Exception {
		String instance = "tacos";
		
		SharedPool<String> pool = new SharedPool<String>();
		
		pool.setInstanceFactory( new MockInstanceFactory(instance ) );
		
		pool.create();
		
		for ( int i = 0 ; i < 100 ; ++i ) {
			assertEquals( instance, pool.borrowInstance() );
		}		
	}
	
	class MockInstanceFactory implements InstanceFactory<String> {

		private String instance;
		
		public MockInstanceFactory(String instance) {
			this.instance = instance;
		}
		
		public String create() throws Exception {
			return this.instance;
		}

		@Override
		public void dispose(String instance) {
			// no-op
		}
		
	}
	
}
