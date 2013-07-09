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

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.projectodd.polyglot.jobs.NotifiableJob;
import org.projectodd.polyglot.jobs.TimeoutListener;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.jobs.component.JobComponent;

public class RubyJobProxy extends NotifiableJob implements Job, StatefulJob, InterruptableJob {

    public RubyJobProxy(RubyRuntimePool runtimePool, ComponentResolver resolver, JobDetail detail) {
        super( detail.getKey() );
        this.runtimePool = runtimePool;
        this.resolver = resolver;
        TimeInterval timeout = (TimeInterval)detail.getJobDataMap().get( "timeout" );
        if (timeout != null) {
            this.addListener( new TimeoutListener( timeout ) );
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Ruby ruby = null;
        
        try {
            ruby = this.runtimePool.borrowRuntime( this.resolver.getComponentName() );
            this.job = (JobComponent)resolver.resolve( ruby );
            notifyStarted( context );
            this.running = true;
            try {
                this.job.run();
            } catch (Exception e) {
                this.job.onError( e );
            } finally {
                RuntimeHelper.evalScriptlet( ruby, "ActiveRecord::Base.clear_active_connections! if defined?(ActiveRecord::Base)" );
            }
            notifyFinished( context );
        } catch (Exception e) {
            notifyError( context, e );
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
            log.warn( "Interrupting job " + this.jobKey );
            try {   
                this.job.onTimeout();
            } catch (Exception e) {
                throw new UnableToInterruptJobException( e );
            }
            notifyInterrupted();
        } else if (this.job == null) {
            log.warn( "Attempted to interrupt job '" + this.jobKey + "' before it started executing." );
        }
    }

    public JobComponent getComponent() {
        return this.job;
    }

    private RubyRuntimePool runtimePool;
    private ComponentResolver resolver;
    private JobComponent job;
    private boolean running = false;
    
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );

}
