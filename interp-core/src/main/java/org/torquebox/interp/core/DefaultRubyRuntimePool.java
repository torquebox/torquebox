/* Copyright 2010 Red Hat, Inc. */
package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.torquebox.common.pool.DefaultPool;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RubyRuntimePool;

public class DefaultRubyRuntimePool extends DefaultPool<Ruby> implements RubyRuntimePool  {

	public DefaultRubyRuntimePool(RubyRuntimeFactory factory) {
		super(factory);
	}

	@Override
	public Ruby borrowRuntime() throws Exception {
		return borrowInstance();
	}

	@Override
	public void returnRuntime(Ruby runtime) {
		releaseInstance( runtime );
	}
	
}
