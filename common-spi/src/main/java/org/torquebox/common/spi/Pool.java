package org.torquebox.common.spi;

public interface Pool<T> {
	
	T borrowInstance() throws Exception;
	void releaseInstance(T instance);

}
