/* Copyright 2009 Red Hat, Inc. */
package org.torquebox.rack.core;

import org.torquebox.common.pool.SharedPool;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;


/** Rack application pool which shares a single instance between consumers.
 * 
 * <p>This pool implementation is backed by a single instance, which is
 * concurrently shared across all consumers.</p>
 * 
 * <p>Multiple threads/requests may be manipulating the application
 * at the same time, so it is important that the application be
 * "thread-safe".</p>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class SharedRackApplicationPool extends SharedPool<RackApplication> implements RackApplicationPool {
	
	/** Construct with a factory.
	 * 
	 * @param factory The factory to create the shared application instance.
	 * @throws Exception
	 */
	public SharedRackApplicationPool(RackApplicationFactory factory) throws Exception {
		super( factory );
	}
	
	/** Construct with an instance.
	 * 
	 * @param app The shared application instance.
	 */
	public SharedRackApplicationPool(RackApplication app) {
		super( app );
	}

	@Override
	public RackApplication borrowApplication() throws Exception {
		return borrowInstance();
	}

	@Override
	public void releaseApplication(RackApplication app) {
		releaseInstance( app );
	}

}
