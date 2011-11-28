package org.torquebox.stomp;

import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

public class MockStartContext implements StartContext {

    private boolean asynchronous;
    private boolean complete;
    private StartException failure;

    @Override
    public void asynchronous() throws IllegalStateException {
        this.asynchronous = true;
    }
    
    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    @Override
    public void complete() throws IllegalStateException {
        this.complete = true;
    }
    
    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public long getElapsedTime() {
        return 0;
    }

    @Override
    public ServiceController<?> getController() {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public void failed(StartException reason) throws IllegalStateException {
        this.failure = reason;
    }
    
    public StartException getFailure() {
        return this.failure;
    }

    @Override
    public ServiceTarget getChildTarget() {
        return null;
    }

}
