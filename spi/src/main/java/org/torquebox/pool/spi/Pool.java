package org.torquebox.pool.spi;

public interface Pool<T> {
	
	T borrowInstance() throws Exception;
	void releaseInstance(T instance);

}
