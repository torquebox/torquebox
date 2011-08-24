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

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class RubyJobProxyFactory implements JobFactory {
       
    @Override
    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {
    	JobDetail jobDetail = bundle.getJobDetail();
        
        ComponentResolver resolver = this.componentResolvers.get( jobDetail.getName() );
        RubyJobProxy rubyJob = new RubyJobProxy( this.runtimePool, resolver );
       
        return rubyJob;
    }
    
    public void addComponentResolver(String rubyClassName, ComponentResolver resolver) {
    	this.componentResolvers.put( rubyClassName, resolver );
    }
    
    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    private RubyRuntimePool runtimePool;
    private Map<String, ComponentResolver> componentResolvers = new HashMap<String, ComponentResolver>();

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );
}
