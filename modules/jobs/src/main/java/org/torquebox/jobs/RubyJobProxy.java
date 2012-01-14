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
import org.quartz.*;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.jobs.component.JobComponent;

public class RubyJobProxy implements Job, StatefulJob, InterruptableJob {

    private Ruby ruby;
    private JobComponent job;

    public RubyJobProxy(RubyRuntimePool runtimePool, ComponentResolver resolver) {
        this.runtimePool = runtimePool;
        this.resolver = resolver;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
         try {
             ruby = this.runtimePool.borrowRuntime( resolver.getComponentName() );
             job = (JobComponent)resolver.resolve( ruby );
             job.run();
         } catch (Exception e) {
        	 throw new JobExecutionException( e );
         } finally {
        	 if (ruby != null) {
        		 this.runtimePool.returnRuntime( ruby );
        	 }
         }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
         log.info("|||||||||||||||||||| Interruption Job  ||||||||||||||||||||");
         try {
             ruby = this.runtimePool.borrowRuntime( resolver.getComponentName() );
             job = (JobComponent)resolver.resolve( ruby );
             job.onTimeout();
         } catch (Exception e) {
        	 throw new UnableToInterruptJobException( e );
         } finally {
        	 if (ruby != null) {
        		 this.runtimePool.returnRuntime( ruby );
        	 }
         }
    }

    private RubyRuntimePool runtimePool;
    private ComponentResolver resolver;

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );



}
