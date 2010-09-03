/* Copyright 2010, Red Hat, Inc. */

package org.torquebox.common.pool;

import org.torquebox.common.spi.InstanceFactory;
import org.torquebox.common.spi.Pool;

/**
 * A pool implementation that shares a single instance to all consumers.
 * 
 * <p>
 * The pool may be primed with either an instance directly or by providing a
 * factory capable of creating the instance. In the case that a
 * {@link InstanceFactory} is used, exactly one instance will be created.
 * </p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @param <T> The poolable resource.
 */
public class SharedPool<T> implements Pool<T> {

	/** Name of the pool. */
	private String name = "anonymous-pool";

	/** The shared instance. */
	private T instance;

	/** Optional factory to create the initial instance. */
	private InstanceFactory<T> factory;

	/**
	 * Construct.
	 */
	public SharedPool() {
	}

	/**
	 * Construct with an instance factory.
	 * 
	 * @param factory The factory to create the initial instance.
	 */
	public SharedPool(InstanceFactory<T> factory) {
		this.factory = factory;
	}

	/**
	 * Construct with an instance.
	 * 
	 * @param instance The initial instance.
	 */
	public SharedPool(T instance) {
		this.instance = instance;
	}

	/**
	 * Set the pool name.
	 * 
	 * @param name The pool name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieve the pool name.
	 * 
	 * @return The pool name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the shared instance.
	 * 
	 * @param instance The initial instance.
	 */
	public void setInstance(T instance) {
		this.instance = instance;
	}

	/**
	 * Retrieve the shared instance.
	 * 
	 * @return The shared instance.
	 */
	public T getInstance() {
		return this.instance;
	}

	/**
	 * Set the instance factory to create the initial instance.
	 * 
	 * @param factory The instance factory.
	 */
	public void setInstanceFactory(InstanceFactory<T> factory) {
		this.factory = factory;
	}

	/**
	 * Retrieve the instance factory to create the initial instance.
	 * 
	 * @return The instance factory;
	 */
	public InstanceFactory<T> getInstanceFactory() {
		return this.factory;
	}

	/**
	 * Create the pool.
	 * 
	 * @throws Exception if an error occurs starting the pool.
	 */
	public void create() throws Exception {
		if (this.instance != null) {
			return;
		}

		if (this.factory == null) {
			throw new IllegalArgumentException("Neither an instance nor an instance-factory provided.");
		}

		this.instance = factory.create();
	}
	
	/** 
	 * Destroy the pool.
	 */
	public void destroy() {
		if ( this.factory != null && this.instance != null ) {
			this.factory.dispose( this.instance );
		}
		this.instance = null;
		this.factory = null;
	}

	@Override
	public T borrowInstance() throws Exception {
		return borrowInstance(0);
	}

	@Override
	public void releaseInstance(T instance) {
		// nothing
	}

	@Override
	public T borrowInstance(long timeout) throws Exception {
		return this.instance;
	}

}
