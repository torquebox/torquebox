package org.torquebox.rack.core;

import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;
import org.torquebox.rack.spi.RackApplicationPool;

public class RackApplicationPoolImpl implements RackApplicationPool {

    private RubyRuntimePool runtimePool;
    private RackApplicationFactory rackFactory;

    public RackApplicationPoolImpl() {
    }

    public RackApplicationPoolImpl(RubyRuntimePool runtimePool, RackApplicationFactory rackFactory) {
        this.runtimePool = runtimePool;
        this.rackFactory = rackFactory;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }

    public void setRackApplicationFactory(RackApplicationFactory rackFactory) {
        this.rackFactory = rackFactory;
    }

    public RackApplicationFactory getRackApplicationFactory() {
        return this.rackFactory;
    }

    @Override
    public RackApplication borrowApplication() throws Exception {
        Ruby ruby = this.runtimePool.borrowRuntime();

        return getRackApplication( ruby );
    }

    @Override
    public void releaseApplication(RackApplication rackApp) {
        this.runtimePool.returnRuntime( rackApp.getRuby() );
    }

    protected RackApplication getRackApplication(Ruby ruby) throws Exception {
        RackApplication rackApp = this.rackFactory.createRackApplication( ruby );
        return rackApp;
    }

}
