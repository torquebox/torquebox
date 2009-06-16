package org.torquebox.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.torquebox.pool.spi.InstanceFactory;
import org.torquebox.pool.spi.Pool;

/**
 * Constrained pool implementation optimized for slow-to-create items.
 * 
 * The first thread to encounter an empty pool pays the price for new object
 * creation. Subsequent threads may exit first due to resources being released.
 * 
 * @author Bob McWhirter
 * 
 * @param <T>
 *            The poolable resource.
 */
public class DefaultPool<T> implements Pool<T> {

	private static final Logger log = Logger.getLogger(DefaultPool.class);
	private InstanceFactory<T> factory;

	private List<T> instances = new ArrayList<T>();
	private Set<T> availableInstances = new HashSet<T>();

	private int minInstances = 0;
	private int maxInstances = -1;
	private int timeout = 30;

	private Semaphore available = new Semaphore(0, true);
	private Semaphore creation = new Semaphore(1);
	private Thread managementThread;

	public DefaultPool(InstanceFactory<T> factory) {
		this.factory = factory;
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
		log.info("starting pool");
		startManagementThread();
	}

	protected void startManagementThread() throws Exception {
		this.managementThread = new Thread() {
			public void run() {
				while (true) {
					synchronized (availableInstances) {
						log.info("acquiring availableInstances lock");
						log.info("acquired availableInstances lock");
						while (!availableInstances.isEmpty()) {
							try {
								log.info("waiting for empty notification");
								availableInstances.wait();
							} catch (InterruptedException e) {
								log.info("management thread exiting");
								return;
							}
						}
						log.info("notified about empty");
						if (availableInstances.isEmpty()) {
							log.info("is still empty");
							if (instances.size() < maxInstances) {
								try {
									log.info("creating instance");
									T instance = factory.create();
									instances.add(instance);
									availableInstances.add(instance);
									available.release();
									log.info("created & released a new instance " + availableInstances.size() + "/"
											+ instances.size() + " = " + available.availablePermits());
								} catch (Exception e) {
									log.error("unable to create instance", e);
								}
							} else {
								try {
									Thread.sleep( 1000 );
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
				log.error("unable to create instance", e);
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
				log.info("asking to create some");
				this.availableInstances.notify();
			}
		}

		log.info("waiting to borrow, pool before [" + availableInstances.size() + " / " + instances.size() + " = "
				+ available.availablePermits() + "]");
		if (available.tryAcquire(this.timeout, TimeUnit.SECONDS)) {
			log.info("borrowing 1, pool minus my permit [" + availableInstances.size() + " / " + instances.size() + " = "
					+ available.availablePermits() + "]");
			Iterator<T> iterator = availableInstances.iterator();
			T instance = iterator.next();
			iterator.remove();
			log.info("borrowed 1, pool minus my instance [" + availableInstances.size() + " / " + instances.size() + " = "
					+ available.availablePermits() + "]");
			return instance;
		}

		return null;
	}

	public synchronized void releaseInstance(T instance) {
		this.availableInstances.add(instance);
		this.available.release();
		log.info("released 1, pool now [" + availableInstances.size() + " / " + instances.size() + " = "
				+ available.availablePermits() + "]");
	}

}
