package org.torquebox.core.injection.analysis;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.torquebox.core.component.InjectionRegistry;

public class RuntimeInjectionListener extends AbstractServiceListener<Object> {

    private static final Callable<Void> NO_OP = new Callable<Void>() {
        public Void call() throws Exception {
            return null;
        }
    };

    RuntimeInjectionListener(InjectionRegistry registry, String key, ClassLoader classLoader) {
        this.registry = registry;
        this.key = key;
        this.classLoader = classLoader;
    }

    @Override
    public void serviceStarted(ServiceController<? extends Object> controller) {
        System.err.println( "DELAYED INJECTION AVOIDING RACISM: " + controller.getValue() );
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader( this.classLoader );
            this.registry.getInjector( this.key ).inject( controller.getValue() );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
        this.lock.run();
        controller.removeListener( this );
    }

    public void waitForInjectionToOccur() throws InterruptedException, ExecutionException {
        this.lock.get();
    }

    private FutureTask<Void> lock = new FutureTask<Void>( NO_OP );

    private ClassLoader classLoader;

    private InjectionRegistry registry;
    private String key;
}