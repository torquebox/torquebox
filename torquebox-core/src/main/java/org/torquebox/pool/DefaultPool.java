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
	private Logger log;

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
		log = Logger.getLogger( this.getClass().getName() + "-" + this.name );
		startManagementThread();
	}

	protected void startManagementThread() throws Exception {
		this.managementThread = new Thread() {
			public void run() {
				while (true) {
					synchronized (availableInstances) {
						log.trace("acquiring availableInstances lock");
						log.trace("acquired availableInstances lock");
						while (!availableInstances.isEmpty()) {
							try {
								log.trace("waiting for empty notification");
								availableInstances.wait();
							} catch (InterruptedException e) {
								log.trace("management thread exiting");
								return;
							}
						}
						log.trace("notified about empty");
						if (availableInstances.isEmpty()) {
							log.trace("is still empty");
							if (instances.size() < maxInstances) {
								try {
									log.trace("creating instance");
									T instance = factory.create();
									instances.add(instance);
									availableInstances.add(instance);
									available.release();
									log.trace("created & released a new instance " + availableInstances.size() + "/"
											+ instances.size() + " = " + available.availablePermits());
								} catch (Exception e) {
									log.error("unable to create instance", e);
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
				log.info( "creating instance for DefaultPool" );
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
				this.availableInstances.notify();
			}
		}

		log.trace("waiting to borrow, pool before [" + availableInstances.size() + " / " + instances.size() + " = "
				+ available.availablePermits() + "]");
		if (available.tryAcquire(this.timeout, TimeUnit.SECONDS)) {
			log.trace("borrowing 1, pool minus my permit [" + availableInstances.size() + " / " + instances.size()
					+ " = " + available.availablePermits() + "]");
			Iterator<T> iterator = availableInstances.iterator();
			T instance = iterator.next();
			iterator.remove();
			log.trace("borrowed 1, pool minus my instance [" + availableInstances.size() + " / " + instances.size()
					+ " = " + available.availablePermits() + "]");
			return instance;
		}

		return null;
	}

	public synchronized void releaseInstance(T instance) {
		this.availableInstances.add(instance);
		this.available.release();
		log.trace("released 1, pool now [" + availableInstances.size() + " / " + instances.size() + " = "
				+ available.availablePermits() + "]");
	}

}
