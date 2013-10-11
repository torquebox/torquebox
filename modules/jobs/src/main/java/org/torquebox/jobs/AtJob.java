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

import org.jboss.msc.inject.Injector;
import org.jboss.msc.value.InjectedValue;
import org.jboss.msc.value.Value;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.projectodd.polyglot.jobs.BaseAtJob;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

import java.text.ParseException;

public class AtJob extends BaseAtJob implements AtJobMBean {
    public AtJob(String group, TimeInterval timeout, String name, String description, boolean singleton) {
        super(RubyJobProxy.class, group, name, description, timeout, singleton);
    }

    @Override
    protected synchronized void _start() throws ParseException, SchedulerException {
        JobScheduler jobScheduler = (JobScheduler) ((Value) getJobSchedulerInjector()).getValue();
        jobScheduler.addComponentResolver(new JobKey(getName(), getGroup()), this.componentResolverInjector.getValue());
        super._start();
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return componentResolverInjector;
    }

    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }

    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
}
