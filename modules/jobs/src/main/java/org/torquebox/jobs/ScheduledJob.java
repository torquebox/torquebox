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

import java.text.ParseException;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class ScheduledJob implements Service<ScheduledJob>, ScheduledJobMBean {
    public static final String RUNTIME_POOL_KEY = "torquebox.ruby.pool";
	
    public ScheduledJob(String group, String name, String description, String cronExpression, boolean singleton, String rubyClassName, String rubyRequirePath) {
    	this.group = group;
    	this.name = name;
    	this.description = description;
    	this.cronExpression = cronExpression;
    	this.singleton = singleton;
    	this.rubyClassName = rubyClassName;
    	this.rubyRequirePath = rubyRequirePath;
    }
    
    @Override
    public ScheduledJob getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        
        context.execute(new Runnable() {
            public void run() {
                try {
                    ScheduledJob.this.start();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        });
    }

    @Override
    public void stop(StopContext context) {
    	stop();
    }
   
   
    public synchronized void start() throws ParseException, SchedulerException {
        this.jobDetail = new JobDetail();

        jobDetail.setGroup( this.group );
        jobDetail.setName( this.name );
        jobDetail.setDescription( this.description );
        jobDetail.setJobClass( RubyJobProxy.class );
        jobDetail.setRequestsRecovery( true );

        JobDataMap jobData = jobDetail.getJobDataMap();
	        
        jobData.put( RubyJobProxyFactory.RUBY_CLASS_NAME_KEY, this.rubyClassName );
        
        CronTrigger trigger = new CronTrigger( getTriggerName(), this.group, this.cronExpression );
        
        JobScheduler jobScheduler = this.jobSchedulerInjector.getValue();
        
        jobScheduler.addComponentResolver( this.rubyClassName, this.componentResolverInjector.getValue() );
        jobScheduler.getScheduler().scheduleJob( jobDetail, trigger );
    }

    public synchronized void stop() {
    	try {
    		this.jobSchedulerInjector.getValue().getScheduler().unscheduleJob( getTriggerName(), this.group );
    	} catch (SchedulerException ex) {
    		log.warn( "An error occurred stoping job " + this.name, ex );
    	} 
    	this.jobDetail = null;	
    }
    

    private String getTriggerName() {
        return this.name + ".trigger";
    }

    public synchronized boolean isStarted() {
        return this.jobDetail != null;
    }
    
    public synchronized boolean isStopped() {
        return this.jobDetail == null;
    }
    
    public synchronized String getStatus() {
        if ( isStarted() ) {
            return "STARTED";
        }
        
        return "STOPPED";
    }

    public String toString() {
        return "[RubyJob: name=" + this.name + "; description=" + this.description + "; rubyClass=" + this.rubyClassName + "]";
    }

    public boolean isSingleton() {
		return singleton;
	}

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }
    
    public void setComponentResolverName(String resolver) {
        this.rubyComponentResolverName = resolver;
    }
    
    public String getComponentResolverName() {
        return this.rubyComponentResolverName;
    }

    public String getRubyClassName() {
        return this.rubyClassName;
    }

    public String getRubyRequirePath() {
        return this.rubyRequirePath;
    }

    public String getDescription() {
        return this.description;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
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
   
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<JobScheduler> jobSchedulerInjector = new InjectedValue<JobScheduler>();
    
    private String group;
    private String name;
    private String description;

    private String rubyClassName;
    private String rubyRequirePath;
    private String rubyComponentResolverName;

    private String cronExpression;

    private RubyRuntimePool runtimePool;
    
    private JobDetail jobDetail;
    private boolean singleton;

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
