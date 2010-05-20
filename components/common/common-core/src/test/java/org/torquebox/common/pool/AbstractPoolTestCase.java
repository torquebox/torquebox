package org.torquebox.common.pool;

import static org.junit.Assert.*;

import org.torquebox.common.spi.ManageablePool;
import org.torquebox.common.spi.Pool;

public abstract class AbstractPoolTestCase {
	
	private long TIMEOUT = 500;

	protected <T> T assertBorrow(Pool<T> pool) throws Exception {
		T instance = pool.borrowInstance();
		assertNotNull( instance );
		return instance;
	}
	
	protected <T> T assertDrain(ManageablePool<T> pool) throws Exception {
		T instance = pool.drainInstance( TIMEOUT );
		assertNotNull( instance );
		return instance;
	}
	
	protected void assertBorrowTimeout(Pool pool) throws Exception {
		long start = System.currentTimeMillis();
		Object instance = pool.borrowInstance( TIMEOUT );
		long stop = System.currentTimeMillis();
		long elapsed = stop - start;
		assertNull( instance );
		assertTrue( elapsed > (TIMEOUT-1000) );
		assertTrue( elapsed < (TIMEOUT+1000) );
	}

}
