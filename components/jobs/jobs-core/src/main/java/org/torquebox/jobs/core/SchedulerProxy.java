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

package org.torquebox.jobs.core;

import java.util.HashMap;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/** 
 * A go-between for ScheduledJobs and a Scheduler that handles the HASingleton 
 * case where the Scheduler only exists on the master node.
 * 
 * @author Tobias Crawley
 *
 */
public class SchedulerProxy {


    public void start(Scheduler scheduler) throws SchedulerException {
        this.scheduler = scheduler;
        scheduleJobs();
    }

    public void stop() throws SchedulerException {
        unscheduleJobs();
        this.scheduler = null;
    }

    public void scheduleJob(String triggerName, JobDetail jobDetail, CronTrigger trigger) throws SchedulerException {
        this.jobs.put( triggerName, new JobData( triggerName, jobDetail, trigger) );
        scheduleJobs();
    }

    public void unscheduleJob(String triggerName) throws SchedulerException {
        JobData job = this.jobs.remove( triggerName );
        unscheduleJob( job );
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected void scheduleJobs() throws SchedulerException {
        if (this.scheduler != null) {
            for(String triggerName : this.jobs.keySet() ) {
                JobData job = this.jobs.get( triggerName );
                if (!job.scheduled) {
                    this.scheduler.scheduleJob( job.jobDetail, job.cronTrigger );
                    job.scheduled = true;
                }
            }
        }
    }

    protected void unscheduleJobs() throws SchedulerException {
        if (this.scheduler != null) {
            for(String triggerName : this.jobs.keySet() ) {
                JobData job = this.jobs.get( triggerName );
                unscheduleJob( job );
                if (!job.scheduled) {
                    this.scheduler.scheduleJob( job.jobDetail, job.cronTrigger );
                    job.scheduled = true;
                }
            }
        }
    }
    
    protected void unscheduleJob(JobData job) throws SchedulerException {
        if (this.scheduler != null && job.scheduled) {
            this.scheduler.unscheduleJob( job.triggerName, job.jobDetail.getGroup() );
            job.scheduled = false;
        }
    }

    private Scheduler scheduler;
    private HashMap<String, JobData> jobs = new HashMap<String, JobData>(); 

    protected class JobData {
        public JobData(String triggerName, JobDetail jobDetail, CronTrigger trigger) {
            this.triggerName = triggerName;
            this.jobDetail = jobDetail;
            this.cronTrigger = trigger;
        }

        public String triggerName;
        public JobDetail jobDetail;
        public CronTrigger cronTrigger;
        public boolean scheduled = false;
    }
}
