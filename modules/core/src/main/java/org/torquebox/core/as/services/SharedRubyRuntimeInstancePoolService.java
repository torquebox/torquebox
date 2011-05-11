package org.torquebox.core.as.services;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.runtime.SharedRubyRuntimePool;

public class SharedRubyRuntimeInstancePoolService implements Service<RubyRuntimePool> {

    public SharedRubyRuntimeInstancePoolService(SharedRubyRuntimePool pool) {
        this.pool = pool;
    }
    
    @Override
    public RubyRuntimePool getValue() throws IllegalStateException, IllegalArgumentException {
        return this.pool;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.pool.setInstance( this.rubyInjector.getValue() );
        try {
            this.pool.create();
        } catch (Exception e) {
            context.failed( new StartException(e) );
        }
    }

    @Override
    public void stop(StopContext context) {
        this.pool.destroy();
        this.pool.setInstance( null );
    }
    
    public Injector<Ruby> getRubyInjector() {
        return this.rubyInjector;
    }

    private InjectedValue<Ruby> rubyInjector = new InjectedValue<Ruby>();
    private SharedRubyRuntimePool pool;

}
