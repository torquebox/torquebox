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
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.torquebox.core.runtime.RubyRuntimePool;

public class ScheduledJob implements ScheduledJobMBean {
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
    
    public synchronized void start() throws ParseException, SchedulerException {
        this.jobDetail = new JobDetail();

        jobDetail.setGroup( this.group );
        jobDetail.setName( this.name );
        jobDetail.setDescription( this.description );
        jobDetail.setJobClass( RubyJobProxy.class );
        jobDetail.setRequestsRecovery( true );

        JobDataMap jobData = jobDetail.getJobDataMap();
	        
        //we need to figure out how to get the component resolver over to the factory
        jobData.put(  RubyJobProxyFactory.COMPONENT_RESOLVER_NAME, getComponentResolverName() );

        jobData.put( RubyJobProxyFactory.RUBY_CLASS_NAME_KEY, this.rubyClassName );
        if ((this.rubyRequirePath != null) && (!this.rubyRequirePath.trim().equals( "" ))) {
            jobData.put( RubyJobProxyFactory.RUBY_REQUIRE_PATH_KEY, this.rubyRequirePath );
        }

        CronTrigger trigger = new CronTrigger( getTriggerName(), this.group, this.cronExpression );
        jobSheduler.getScheduler().scheduleJob( jobDetail, trigger );
    }

    private String getTriggerName() {
        return this.name + ".trigger";
    }

    public synchronized void stop() {
    	try {
    		jobSheduler.getScheduler().unscheduleJob( getTriggerName(), this.group );
    	} catch (SchedulerException ex) {
    		log.warn( "An error occurred stoping job " + this.name, ex );
    	} 
    	this.jobDetail = null;
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

    public void setJobScheduler(JobScheduler scheduler) {
        this.jobSheduler = scheduler;
    }

    public JobScheduler getJobScheduler() {
        return this.jobSheduler;
    }

    private String group;
    private String name;
    private String description;

    private String rubyClassName;
    private String rubyRequirePath;
    private String rubyComponentResolverName;

    private String cronExpression;

    private RubyRuntimePool runtimePool;
    private JobScheduler jobSheduler;

    private JobDetail jobDetail;
    private boolean singleton;

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
