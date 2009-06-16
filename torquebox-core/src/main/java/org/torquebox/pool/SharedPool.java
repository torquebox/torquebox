package org.torquebox.pool;

import org.torquebox.pool.spi.InstanceFactory;
import org.torquebox.pool.spi.Pool;

public class SharedPool<T> implements Pool<T> {
	
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
	
	public void setFactory(InstanceFactory<T> factory) {
		this.factory = factory;
	}

	@Override
	public T borrowInstance() throws Exception {
		if ( this.instance == null ) {
			if ( this.factory == null ) {
				throw new Exception( "Unable to create instance" );
			} else {
				this.instance = this.factory.create();
			}
		}
		
		return this.instance;
	}

	@Override
	public void releaseInstance(T instance) {
		// nothing
	}

}
