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

package org.torquebox.jobs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.polyglot.jobs.BaseJobScheduler;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.JobKey;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class JobScheduler extends BaseJobScheduler implements JobFactory {

    public JobScheduler(String name, int threadCount) {
        super(name, threadCount);
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();

        ComponentResolver resolver = this.componentResolvers.get(jobDetail.getKey());
        if (resolver == null) {
            log.errorf("JobScheduler.newJob found no ComponentResolver for %s", jobDetail.getKey());
        }
        RubyJobProxy rubyJob = new RubyJobProxy(this.rubyRuntimePoolInjector.getValue(), resolver, jobDetail);

        return rubyJob;
    }

    public void start() throws IOException, SchedulerException {
        setJobFactory(this);
        super.start();
    }

    public void addComponentResolver(JobKey key, ComponentResolver resolver) {
        log.tracef("JobScheduler.addComponentResolver for %s with resolver %s", key, resolver);
        this.componentResolvers.put(key, resolver);
    }

    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }

    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private Map<JobKey, ComponentResolver> componentResolvers = new HashMap<JobKey, ComponentResolver>();

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");
}
