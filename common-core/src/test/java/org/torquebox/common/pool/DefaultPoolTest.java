package org.torquebox.common.pool;

import java.util.concurrent.TimeUnit;

import org.torquebox.common.spi.InstanceFactory;

import junit.framework.TestCase;


public class DefaultPoolTest extends TestCase {

	class MockInstanceFactory implements InstanceFactory<String> {

		private int counter = 0;

		@Override
		public String create() throws Exception {
			return "Instance-" + (++counter);
		}

	}

	private MockInstanceFactory instanceFactory;
	private ConstrainedPool<String> pool;

	public void setUp() throws Exception {
		this.instanceFactory = new MockInstanceFactory();
		this.pool = new ConstrainedPool<String>(this.instanceFactory);
	}

	public void tearDown() throws Exception {

	}

	public void testDefaultPool() throws Exception {

		assertEquals(1, pool.getMinInstances());
		assertEquals(1, pool.getMaxInstances());
	}

	public void testUnityPool() throws Exception {

		pool.setMinInstances(1);
		pool.setMaxInstances(1);

		pool.start();

		String instance1 = pool.borrowInstance();

		assertNotNull(instance1);

		String instance2 = pool.borrowInstance(2, TimeUnit.SECONDS);

		assertNull(instance2);

		pool.releaseInstance(instance1);

		instance2 = pool.borrowInstance();

		assertNotNull(instance2);

		assertEquals("Instance-1", instance2);
	}

	public void testMinimumInstanceCreation() throws Exception {
		pool.setMinInstances(10);
		pool.setMaxInstances(20);

		pool.start();

		int i = 0;
		while (pool.getInstanceCount() < 10) {
			++i;
			Thread.sleep(100);
			if (i > 20) {
				fail("minimum instance creation taking too long");
			}
		}

		assert (pool.getInstanceCount() >= pool.getMinInstances());
		assert (pool.getInstanceCount() <= pool.getMaxInstances());
	}

	public void testMaxGreaterThanMin() throws Exception {
		pool.setMinInstances(10);
		pool.setMaxInstances(1);

		try {
			pool.start();
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected and correct
		}
	}

	public void testMinAndMaxNotOneButEqual() throws Exception {

		pool.setMinInstances(10);
		pool.setMaxInstances(10);

		pool.start();

		int i = 0;
		while (pool.getInstanceCount() < 10) {
			++i;
			Thread.sleep(100);
			if (i > 20) {
				fail("minimum instance creation taking too long");
			}
		}

		Thread.sleep(1000);
		assertEquals(10, pool.getInstanceCount());
		Thread.sleep(1000);
		assertEquals(10, pool.getInstanceCount());

	}

	public void testGrowingPool() throws Exception {

		pool.setMinInstances(10);
		pool.setMaxInstances(20);

		pool.start();

		int i = 0;
		while (pool.getInstanceCount() < 10) {
			++i;
			Thread.sleep(100);
			if (i > 20) {
				fail("minimum instance creation taking too long");
			}
		}

		assertEquals(10, pool.getInstanceCount());
		assertEquals(10, pool.getAvailableInstanceCount());

		String[] instances = new String[20];

		for (int j = 0; j < 9; ++j) {
			instances[j] = pool.borrowInstance();
			assertNotNull(instances[j]);
			assertEquals(10, pool.getInstanceCount());
			assertEquals(10 - (j + 1), pool.getAvailableInstanceCount());
		}

		instances[9] = pool.borrowInstance();
		assertEquals(10, pool.getInstanceCount());

		instances[10] = pool.borrowInstance();
		assertEquals(11, pool.getInstanceCount());
		assertEquals(0, pool.getAvailableInstanceCount());

		for (int j = 11; j < 20; ++j) {
			instances[j] = pool.borrowInstance();
			assertNotNull(instances[j]);
			assertEquals(j + 1, pool.getInstanceCount());
			assertEquals(0, pool.getAvailableInstanceCount());
		}
	}

}
