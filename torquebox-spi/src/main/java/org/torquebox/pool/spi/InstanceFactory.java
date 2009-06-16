package org.torquebox.pool.spi;

public interface InstanceFactory<T> {
	
	T create() throws Exception;

}
