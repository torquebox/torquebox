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

import java.io.IOException;
import java.util.Properties;

import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyScheduler {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( RubyScheduler.class );

    private String name;
    private Scheduler scheduler;
    private SchedulerProxy schedulerProxy;
    private Kernel kernel;
    private RubyRuntimePool runtimePool;
    
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public Kernel getKernel() {
        return this.kernel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public SchedulerProxy getSchedulerProxy() {
        return schedulerProxy;
    }

    public void setSchedulerProxy(SchedulerProxy schedulerProxy) {
        this.schedulerProxy = schedulerProxy;
    }
	
    public void start() throws IOException, SchedulerException {
        Properties props = new Properties();
        props.load( this.getClass().getResourceAsStream( "scheduler.properties" ) );
        props.setProperty( StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, getName() );

        RubyJobProxyFactory jobFactory = new RubyJobProxyFactory();
        jobFactory.setKernel( this.kernel );
        jobFactory.setRubyRuntimePool( this.runtimePool );

        StdSchedulerFactory factory = new StdSchedulerFactory( props );
        this.scheduler = factory.getScheduler();
        this.scheduler.setJobFactory( jobFactory );
        this.scheduler.start();
        
        if (this.schedulerProxy != null) {
            this.schedulerProxy.start( this.scheduler );
        }
    }

    public void stop() throws SchedulerException {
        if (this.schedulerProxy != null) {
            this.schedulerProxy.stop();
        }
        
        this.scheduler.shutdown( true );
    }

}
