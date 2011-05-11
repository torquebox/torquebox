package org.torquebox.web.rack;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.runtime.RubyRuntimePool;

public class RackApplicationPoolService implements Service<RackApplicationPool> {

    public RackApplicationPoolService(RackApplicationPoolImpl pool) {
        this.pool = pool;
    }

    @Override
    public RackApplicationPool getValue() throws IllegalStateException, IllegalArgumentException {
        return this.pool;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info( "Staring pool" );
        this.pool.setRubyRuntimePool( this.rubyRuntimePoolInjector.getValue() );
        this.pool.setRackApplicationFactory( this.rackApplicationFactoryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        log.info( "Stopping pool" );
        this.pool.setRackApplicationFactory( null );
        this.pool.setRubyRuntimePool( null );
    }

    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }
    
    public Injector<RackApplicationFactory> getRackApplicationFactoryInjector() {
        return this.rackApplicationFactoryInjector;
    }

    private RackApplicationPoolImpl pool;
    private InjectedValue<RackApplicationFactory> rackApplicationFactoryInjector = new InjectedValue<RackApplicationFactory>();
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack.pool" );
}
