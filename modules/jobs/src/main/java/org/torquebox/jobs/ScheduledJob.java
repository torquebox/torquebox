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

import java.text.ParseException;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.value.InjectedValue;
import org.jboss.msc.value.Value;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.projectodd.polyglot.jobs.BaseScheduledJob;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class ScheduledJob extends BaseScheduledJob implements ScheduledJobMBean {
    public static final String RUNTIME_POOL_KEY = "torquebox.ruby.pool";

    public ScheduledJob(String group, String name, String description, String cronExpression, TimeInterval timeout, boolean singleton, boolean stopped, String rubyClassName) {
        super( RubyJobProxy.class, group, name, description, cronExpression, timeout, singleton, stopped );
        this.rubyClassName = rubyClassName;
    }

    @Override
    protected synchronized void _start() throws ParseException, SchedulerException {
        waitForMSCServiceToStart();
        if (!isStarted()) {
            JobScheduler jobScheduler = (JobScheduler)((Value)getJobSchedulerInjector()).getValue();
            jobScheduler.addComponentResolver(new JobKey(getName(), getGroup()), this.componentResolverInjector.getValue());
            super._start();
        }
    }

    /**
     * Ensure this ScheduledJob's MSC service has started (signalled by the
     * presence of all injected values) before returning
     */
    protected void waitForMSCServiceToStart() {
        while (((InjectedValue)this.getJobSchedulerInjector()).getOptionalValue() == null ||
                this.componentResolverInjector.getOptionalValue() == null ||
                this.rubyRuntimePoolInjector.getOptionalValue() == null) {
            try {
                Thread.sleep( 10 );
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public String getRubyClassName() {
        return rubyClassName;
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }

    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }

    private String rubyClassName;
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
