/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.gem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceController.Substate;
import org.jboss.msc.service.ServiceController.Transition;

public class MSCServiceListener extends AbstractServiceListener<Object> {

    public MSCServiceListener(ServiceController<?> controller) {
        this.controller = controller;
    }

    @Override
    public void transition(ServiceController<? extends Object> controller, Transition transition) {
        if (transition.getAfter() == Substate.UP ||
            (transition.getAfter() != Substate.START_REQUESTED &&
             transition.getAfter() != Substate.START_INITIATING &&
             transition.getAfter() != Substate.STARTING)) {
            // we've either started or are not going to start (start
            // failed, shutting down
            notifyAsStartedOrProblem();
            controller.removeListener(this);
        }
    }

    void notifyAsStartedOrProblem() {
        this.startLatch.countDown();
    }

    public boolean waitForStartOrFailure(long secondsToWait) throws InterruptedException {
        if (isUp()) {
            return true;
        }
        this.startLatch.await(secondsToWait, TimeUnit.SECONDS);
        if (isUp()) {
            return true;
        }
        return false;
    }

    public boolean isUp() {
        return (controller.getState() == State.UP);
    }

    public Object getValue() {
        return this.controller.getValue();
    }

    public String toString() {
        return "[MSCServiceListener: controller=" + this.controller +
            "; state=" + this.controller.getState() +
            "; substate=" + this.controller.getSubstate() + "]";
    }

    private CountDownLatch startLatch = new CountDownLatch(1);

    private ServiceController<?> controller;
}
