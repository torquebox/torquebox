/* Copyright 2010, Red Hat, Inc. */

package org.torquebox.common.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.torquebox.common.spi.InstanceFactory;
import org.torquebox.common.spi.Pool;

/**
 * Constrained pool implementation optimized for slow-to-create items.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @param <T> The poolable resource.
 */
public class ConstrainedPool<T> implements Pool<T> {

	/** Instance factory. */
	private InstanceFactory<T> factory;

	/** All instances. */
	private List<T> instances = new ArrayList<T>();

	/** All available instances. */
	private Set<T> availableInstances = new HashSet<T>();

	/** Factory name. */
	private String name = "anonymous";

	/** Minimum number of instances in the pool. */
	private int minInstances = 1;

	/** Maximum number of instances in the pool. */
	private int maxInstances = 1;

	/** Timeout for waiting to acquire an instance. */
	private long timeout = 30;

	/** Timeout units. */
	private TimeUnit timeoutUnit = TimeUnit.SECONDS;

	/** Semaphore for safely tracking available instances. */
	private Semaphore available = new Semaphore(0, true);

	/** Pool population management thread. */
	private Thread managementThread;

	/**
	 * Construct.
	 * 
	 * @param factory The instance factory.
	 */
	public ConstrainedPool(InstanceFactory<T> factory) {
		this.factory = factory;
	}
	
	/** Retrieve the instance factory.
	 * 
	 * @return The instance factory.
	 */
	public InstanceFactory<T> getInstanceFactory() {
		return this.factory;
	}

	/**
	 * Set the name.
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Retrieve the minimum number of instances in the pool.
	 * 
	 * @return The minimum number of instances in the pool.
	 */
	public int getMinInstances() {
		return this.minInstances;
	}

	/**
	 * Set the minimum number of instances in the pool.
	 * 
	 * @param minInstances The minimum number of instances in the pool.
	 */
	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	/**
	 * Retrieve the maximun number of instances in the pool.
	 * 
	 * @return The maximum number of instances in the pool.
	 */
	public int getMaxInstances() {
		return this.maxInstances;
	}

	/**
	 * Set the maximum number of instances in the pool.
	 * 
	 * @param maxInstances The maximum number of instances in the pool.
	 */
	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}

	/**
	 * Time-out to fetch an instance.
	 * 
	 * @param timeout Time-out length, in seconds.
	 */
	public void setTimeout(long timeout, TimeUnit timeoutUnit) {
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
	}

	/** Start the pool
	 * 
	 * <p>Starts the population management thread.
	 * 
	 * @throws Exception if an error occurs starting the pool.
	 */
	public synchronized void start() throws Exception {
		if (this.minInstances > this.maxInstances) {
			throw new IllegalArgumentException("minInstances may not be greater than maxInstances");
		}

		startManagementThread();
	}

	/** Retrieve the number of available instances.
	 * 
	 * @return The number of available instances.
	 */
	public int getAvailableInstanceCount() {
		synchronized (availableInstances) {
			return this.availableInstances.size();
		}
	}

	/** Retrieve the total number of instances (available and in-use).
	 * 
	 * @return The total number of instances.
	 */
	public int getInstanceCount() {
		synchronized (availableInstances) {
			return this.instances.size();
		}
	}

	/** Start population management thread.
	 * 
	 * @throws Exception if an error occurs starting the thread.
	 */
	protected void startManagementThread() throws Exception {
		this.managementThread = new Thread() {
			public void run() {
				while (true) {
					synchronized (availableInstances) {
						while (!availableInstances.isEmpty()) {
							try {
								availableInstances.wait();
							} catch (InterruptedException e) {
								return;
							}
						}
						if (availableInstances.isEmpty()) {
							if (instances.size() < maxInstances) {
								try {
									T instance = factory.create();
									instances.add(instance);
									availableInstances.add(instance);
									available.release();
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									return;
								}
							}
						}
					}
				}
			}
		};

		while (instances.size() < minInstances) {
			try {
				T instance = factory.create();
				instances.add(instance);
				availableInstances.add(instance);
				available.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.managementThread.start();
	}

	/** Stop the pool.
	 */
	public void stop() {
		this.managementThread.interrupt();
		this.instances.clear();
		this.availableInstances.clear();
		this.available = new Semaphore(0, true);
	}

	/** Borrow an instance with default timeout.
	 * 
	 * @return The instance borrowed.
	 * 
	 * @throws Exception if unable to obtain an instance.
	 * 
	 * @see #borrowInstance(long, TimeUnit)
	 */
	public T borrowInstance() throws Exception {
		return borrowInstance(this.timeout, this.timeoutUnit);
	}

	/** Borrow an instance with a specified timeout.
	 * 
	 * @param timeout The length of timeout to wait for an instance.
	 * @param timeoutUnit The units for the timeout parameter.
	 * @return The instance borrowed.
	 * @throws Exception if unable to object an instance.
	 */
	public T borrowInstance(long timeout, TimeUnit timeoutUnit) throws Exception {

		synchronized (this.availableInstances) {
			if (this.availableInstances.isEmpty()) {
				this.availableInstances.notify();
			}
		}

		if (available.tryAcquire(timeout, timeoutUnit)) {
			Iterator<T> iterator = availableInstances.iterator();
			T instance = iterator.next();
			iterator.remove();
			return instance;
		}

		return null;
	}

	/** Release a borrowed instance.
	 * 
	 * @param instance The instance to return.
	 */
	public synchronized void releaseInstance(T instance) {
		this.availableInstances.add(instance);
		this.available.release();
	}

}
