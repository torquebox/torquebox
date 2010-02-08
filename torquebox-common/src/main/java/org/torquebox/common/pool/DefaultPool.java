package org.torquebox.common.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.torquebox.pool.spi.InstanceFactory;
import org.torquebox.pool.spi.Pool;

/**
 * Constrained pool implementation optimized for slow-to-create items.
 * 
 * @author Bob McWhirter
 * 
 * @param <T> The poolable resource.
 */
public class DefaultPool<T> implements Pool<T> {

	private InstanceFactory<T> factory;

	private List<T> instances = new ArrayList<T>();
	private Set<T> availableInstances = new HashSet<T>();

	private String name = "anonymous";
	private int minInstances = 0;
	private int maxInstances = -1;
	private int timeout = 30;

	private Semaphore available = new Semaphore(0, true);
	private Thread managementThread;

	public DefaultPool(InstanceFactory<T> factory) {
		this.factory = factory;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}

	/**
	 * Time-out to fetch an instance.
	 * 
	 * @param timeout
	 *            Time-out length, in seconds.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public synchronized void start() throws Exception {
		startManagementThread();
	}

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

	public void stop() {
		this.managementThread.interrupt();
		this.instances.clear();
		this.availableInstances.clear();
		this.available = new Semaphore(0, true);
	}

	public T borrowInstance() throws Exception {

		synchronized (this.availableInstances) {
			if (this.availableInstances.isEmpty()) {
				this.availableInstances.notify();
			}
		}

		if (available.tryAcquire(this.timeout, TimeUnit.SECONDS)) {
			Iterator<T> iterator = availableInstances.iterator();
			T instance = iterator.next();
			iterator.remove();
			return instance;
		}

		return null;
	}

	public synchronized void releaseInstance(T instance) {
		this.availableInstances.add(instance);
		this.available.release();
	}

}
