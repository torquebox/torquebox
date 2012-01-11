/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.jobs;

import org.jboss.logging.Logger;
import org.quartz.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RubyTriggerListener implements TriggerListener {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void triggerFired(Trigger trigger, final JobExecutionContext jobExecutionContext) {
        // TODO include some action here when trigger starts
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, final JobExecutionContext jobExecutionContext) {

        RubyTriggerListener.registerWatchDog(jobExecutionContext);

        return true;
    }

    private static void registerWatchDog(final JobExecutionContext jobExecutionContext) {

        //int delay = (Integer) jobExecutionContext.getJobDetail().getJobDataMap().get("timeout");
        int delay = 5000; //will be changed on polyglot
        //TODO Replace ExecutorService by JBossThreadPool
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        service.schedule(new Runnable() {
            public void run() {

                log.info("|||||||||||||||| Trying to interrupt the job |||||||||||||||| ");
                try {
                    ((InterruptableJob) jobExecutionContext.getJobInstance()).interrupt();
                } catch (Exception e) {
                    log.error("Interruption failed", e);
                }


            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // TODO include some action here
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, int i) {
        // TODO include some action here
    }

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");


}
