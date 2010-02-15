package org.torquebox.common.pool;

import org.torquebox.pool.spi.InstanceFactory;
import org.torquebox.pool.spi.Pool;

public class SharedPool<T> implements Pool<T> {
	
	
	private String name = "anonymous-pool";
	private T instance;
	private InstanceFactory<T> factory;
	
	public SharedPool() {
	}
	
	public SharedPool(InstanceFactory<T> factory) {
		this.factory = factory;
	}
	
	public SharedPool(T instance) {
		this.instance = instance;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setFactory(InstanceFactory<T> factory) {
		this.factory = factory;
	}
	
	public void start() throws Exception {
		if ( this.instance != null ) {
			return;
		}
		
		if ( this.factory != null ) {
			this.instance = factory.create();
		}
	}

	@Override
	public T borrowInstance() throws Exception {
		return this.instance;
	}

	@Override
	public void releaseInstance(T instance) {
		// nothing
	}

}
