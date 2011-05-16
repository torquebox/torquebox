package org.torquebox.core.as.services;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.runtime.DefaultRubyRuntimePool;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.runtime.RubyRuntimePool;

public class DefaultRubyRuntimePoolStartService implements Service<RubyRuntimePool> {

    public DefaultRubyRuntimePoolStartService(DefaultRubyRuntimePool pool) {
        this.pool = pool;
    }

    @Override
    public RubyRuntimePool getValue() throws IllegalStateException, IllegalArgumentException {
        return pool;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        context.execute( new Runnable() {
            @Override
            public void run() {
                try {
                    DefaultRubyRuntimePoolStartService.this.pool.start();
                    context.complete();
                } catch (InterruptedException e) {
                    context.failed( new StartException( e ) );
                }
            }
        } );
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.pool.stop();
        } catch (InterruptedException e) {
            // swallow
        }
    }

    public Injector<RubyRuntimeFactory> getRubyRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }

    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();
    private DefaultRubyRuntimePool pool;

}
