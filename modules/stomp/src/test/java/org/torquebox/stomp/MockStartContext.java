/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
