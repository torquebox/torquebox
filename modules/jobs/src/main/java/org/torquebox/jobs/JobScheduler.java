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

import java.io.IOException;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.torquebox.core.runtime.RubyRuntimePool;

public class JobScheduler implements Service<JobScheduler> {

	public JobScheduler(String name) {
		this.name = name;
	}
	
	@Override	
	public JobScheduler getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}	

	@Override
	public void start(final StartContext context) throws StartException {
		context.asynchronous();
	        
		context.execute(new Runnable() {
			public void run() {
				try {
					JobScheduler.this.start();
					context.complete();
            	} catch (Exception e) {
            		context.failed( new StartException( e ) );
            	}
            }
        });
    }

    @Override
    public void stop(StopContext context) {
    	try {
    		this.scheduler.shutdown( true );
    	} catch (SchedulerException ex) {
		log.warn( "An error occured stopping scheduler for " + this.name, ex );
    	}
    }
	   
    public void start() throws IOException, SchedulerException {
        Properties props = new Properties();
        props.load( this.getClass().getResourceAsStream( "scheduler.properties" ) );
        props.setProperty( StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, getName() );

        RubyJobProxyFactory jobFactory = new RubyJobProxyFactory();
        jobFactory.setRubyRuntimePool( this.rubyRuntimePoolInjector.getValue() );

        StdSchedulerFactory factory = new StdSchedulerFactory( props );
        this.scheduler = factory.getScheduler();
        this.scheduler.setJobFactory( jobFactory );
        this.scheduler.start();
    }

    public String getName() {
        return this.name;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }
       
    public Injector<RubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }
    
    private String name;
    private Scheduler scheduler;
        
    private InjectedValue<RubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RubyRuntimePool>();
    
	private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
