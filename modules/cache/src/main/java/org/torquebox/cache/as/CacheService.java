package org.torquebox.cache.as;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class CacheService implements Service<CacheService> {

	@Override
	public CacheService getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}

	@Override
	public void start(StartContext context) throws StartException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(StopContext context) {
		// TODO Auto-generated method stub

	}

}
