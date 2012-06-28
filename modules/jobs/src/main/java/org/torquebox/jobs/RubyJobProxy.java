/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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
import org.jruby.Ruby;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.jobs.component.JobComponent;

public class RubyJobProxy implements Job, StatefulJob, InterruptableJob {

    public RubyJobProxy(RubyRuntimePool runtimePool, ComponentResolver resolver, String jobName) {
        this.runtimePool = runtimePool;
        this.resolver = resolver;
        this.jobName = jobName;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Ruby ruby = null;
        
        try {
            ruby = this.runtimePool.borrowRuntime( this.resolver.getComponentName() );
            this.job = (JobComponent)resolver.resolve( ruby );
            this.running = true;
            this.job.run();
        } catch (Exception e) {
            throw new JobExecutionException( e );
        } finally {
            if (ruby != null) {
                this.runtimePool.returnRuntime( ruby );
            }
            this.running = false;
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (this.running) {
            log.warn( "Interrupting job " + this.jobName );
            try {   
                this.job.onTimeout();
            } catch (Exception e) {
                throw new UnableToInterruptJobException( e );
            }
        } else if (this.job == null) {
            log.warn( "Attempted to interrupt job '" + this.jobName + "' before it started executing." );
        }
    }

    private RubyRuntimePool runtimePool;
    private ComponentResolver resolver;
    private JobComponent job;
    private String jobName;
    private boolean running = false;
    
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );

}
