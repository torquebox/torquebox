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

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class ScheduledJobProxy implements Service<ScheduledJob> {
    
    public ScheduledJobProxy(ScheduledJob scheduler) {
        this.job = scheduler;
    }

    @Override
    public ScheduledJob getValue() throws IllegalStateException, IllegalArgumentException {
        return this.job;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        
        this.job.setRubyRuntimePool( this.rubyRuntimePoolInjector.getValue() );
        //this.job.setComponentResolverName( this.componentResolverInjector.getValue() );
        this.job.setJobScheduler( this.jobSchedulerInjector.getValue() );
        
        context.execute(new Runnable() {
            public void run() {
                try {
                    ScheduledJobProxy.this.job.start();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        });
    }

    @Override
    public void stop(StopContext context) {
        this.job.stop();
    }
   
    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }
   
    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }
   
    public Injector<JobScheduler> getJobSchedulerInjector() {
        return this.jobSchedulerInjector;
    }
   
    private ScheduledJob job;
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<JobScheduler> jobSchedulerInjector = new InjectedValue<JobScheduler>();
}
