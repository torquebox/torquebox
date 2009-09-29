package org.torquebox.ruby.enterprise.web.rack;

import org.torquebox.pool.DefaultPool;
import org.torquebox.ruby.enterprise.web.rack.spi.RackApplication;
import org.torquebox.ruby.enterprise.web.rack.spi.RackApplicationFactory;
import org.torquebox.ruby.enterprise.web.rack.spi.RackApplicationPool;

public class DefaultRackApplicationPool extends DefaultPool<RackApplication> implements RackApplicationPool {
	
	public DefaultRackApplicationPool(RackApplicationFactory factory) {
		super( factory );
	}

	@Override
	public RackApplication borrowApplication() throws Exception {
		log.info( "borrowApplication" );
		return borrowInstance();
	}

	@Override
	public void releaseApplication(RackApplication app) {
		log.info( "releaseApplication" );
		releaseInstance( app );
	}

}
