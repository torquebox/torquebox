package org.torquebox.core.as.services;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.runtime.SharedRubyRuntimePool;

public class SharedRubyRuntimeFactoryPoolService implements Service<RubyRuntimePool> {

    public SharedRubyRuntimeFactoryPoolService(SharedRubyRuntimePool pool) {
        this.pool = pool;
    }

    @Override
    public RubyRuntimePool getValue() throws IllegalStateException, IllegalArgumentException {
        return this.pool;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        this.pool.setInstanceFactory( factoryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.pool.setInstanceFactory( null );
    }

    public Injector<RubyRuntimeFactory> getRubyRuntimeFactoryInjector() {
        return this.factoryInjector;
    }

    private InjectedValue<RubyRuntimeFactory> factoryInjector = new InjectedValue<RubyRuntimeFactory>();
    private SharedRubyRuntimePool pool;

}
