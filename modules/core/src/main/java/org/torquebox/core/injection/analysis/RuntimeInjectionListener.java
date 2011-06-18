package org.torquebox.core.injection.analysis;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.State;

public class RuntimeInjectionListener extends AbstractServiceListener<Object> {

    private static final Callable<Void> NO_OP = new Callable<Void>() {
        public Void call() throws Exception {
            return null;
        }
    };

    RuntimeInjectionListener(ServiceController<?> controller, String key) {
        this.controller = controller;
        this.key = key;
    }

    String getKey() {
        return this.key;
    }

    @Override
    public void serviceStarted(ServiceController<? extends Object> controller) {
        notifyAsInjectable();
        controller.removeListener( this );
    }

    void notifyAsInjectable() {
        this.lock.run();
    }

    public void waitForInjectableness() throws InterruptedException, ExecutionException {
        if (isUp()) {
            return;
        }
        this.lock.get();
    }

    public boolean isUp() {
        return (controller.getState() == State.UP);
    }

    public Object getValue() {
        return this.controller.getValue();
    }

    public String toString() {
        return "[RuntimeInjectionListener: controller=" + this.controller + "; state=" + this.controller.getState() + "]";
    }

    private FutureTask<Void> lock = new FutureTask<Void>( NO_OP );

    private ServiceController<?> controller;
    private String key;
}