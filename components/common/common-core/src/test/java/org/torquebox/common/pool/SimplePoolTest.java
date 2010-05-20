package org.torquebox.common.pool;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.torquebox.common.spi.PoolListener;

public class SimplePoolTest extends AbstractPoolTestCase {

	@Test
	public void testEmptyPool() throws Exception {
		SimplePool<String> pool = new SimplePool<String>();
		assertBorrowTimeout( pool );
	}
	
	@Test
	public void testFillBorrowReleaseDrain() throws Exception {
		
		SimplePool<String> pool = new SimplePool<String>();
		assertBorrowTimeout( pool );
		
		pool.fillInstance( "Instance-1" );
		pool.fillInstance( "Instance-2" );
		pool.fillInstance( "Instance-3" );
		
		Set<String> instances = new HashSet<String>();
		
		instances.add( assertBorrow( pool ) );
		instances.add( assertBorrow( pool ) );
		instances.add( assertBorrow( pool ) );
		
		assertTrue( instances.contains( "Instance-1" ) );
		assertTrue( instances.contains( "Instance-2" ) );
		assertTrue( instances.contains( "Instance-3" ) );
		
		assertBorrowTimeout( pool );
		
		pool.releaseInstance( "Instance-2" );
		
		assertEquals( "Instance-2", assertBorrow( pool ) );
		
		pool.releaseInstance( "Instance-1" );
		pool.releaseInstance( "Instance-2" );
		pool.releaseInstance( "Instance-3" );
		
		instances.clear();
		
		instances.add( assertDrain( pool ) );
		instances.add( assertDrain( pool ) );
		instances.add( assertDrain( pool ) );
		
		assertTrue( instances.contains( "Instance-1" ) );
		assertTrue( instances.contains( "Instance-2" ) );
		assertTrue( instances.contains( "Instance-3" ) );
		
		assertBorrowTimeout(pool);
	}
	
	@Test
	public void testListenerCallbacks() throws Exception {
		SimplePool<String> pool = new SimplePool<String>();
		
		PoolListener<String> listener = mock( PoolListener.class );
		pool.addListener( listener );
		
		pool.fillInstance( "Instance-1" );
		
		assertBorrow( pool );
		
		verify( listener ).instanceRequested(1, 1);
		verify( listener ).instanceBorrowed("Instance-1", 1, 0);
		
		pool.releaseInstance( "Instance-1" );
		
		verify( listener ).instanceReleased("Instance-1", 1, 1);
	}
	

}
