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

package org.torquebox.core.injection.analysis;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceController.Substate;
import org.jboss.msc.service.ServiceController.Transition;

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
    public void transition(ServiceController<? extends Object> controller, Transition transition) {
        if (transition.getAfter() == Substate.UP) {
            notifyAsInjectable();
            controller.removeListener( this );
        }
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
