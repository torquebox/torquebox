/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.rack.core;

import org.torquebox.common.pool.DefaultPool;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;

/** Typical non-shared, constrained Ruby-Rack application pool.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class DefaultRackApplicationPool extends DefaultPool<RackApplication> implements RackApplicationPool {
	
	/** Construct with a factory.
	 * 
	 * @param factory The application factory.
	 */
	public DefaultRackApplicationPool(RackApplicationFactory factory) {
		super( factory );
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
