/** Copyright 2010 Red Hat, Inc. */
package org.torquebox.common.spi;

/** A simple instance pool.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 *
 * @param <T> The instance type.
 */
public interface Pool<T> {
	
	/** Borrow an instance from the pool.
	 * 
	 * @return The borrowed instance.
	 * @throws Exception if an error occurs.
	 */
	T borrowInstance() throws Exception;
	
	/** Borrow an instance from the pool.
	 * 
	 * @param timeout Wait time to acquire instance.
	 * @return The borrowed instance.
	 * @throws Exception if an error occurs.
	 */
	T borrowInstance(long timeout) throws Exception;
	
	/** Release an instance back into the pool.
	 * 
	 * @param instance The instance to release.
	 */
	void releaseInstance(T instance);

}
