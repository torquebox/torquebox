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

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyJobFactory implements JobFactory {

    public static final String RUBY_CLASS_NAME_KEY = "torquebox.ruby.class.name";
    public static final String RUBY_REQUIRE_PATH_KEY = "torquebox.ruby.require.path";
    public static final String COMPONENT_RESOLVER_NAME = "torquebox.ruby.component.resolver.name";

    private RubyRuntimePool runtimePool;
    private boolean alwaysReload;
    private Kernel kernel;

    public RubyJobFactory(boolean reload) {
        this.alwaysReload = reload;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePool;
    }
    
    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }
    
    public Kernel getKernel() {
        return this.kernel;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {
        RubyJob rubyJob = null;

        JobDetail jobDetail = bundle.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        
        RubyComponentResolver resolver = getComponentResolver( jobDataMap.getString(   RubyJobFactory.COMPONENT_RESOLVER_NAME ));

        /*
        RubyComponentResolver resolver = new RubyComponentResolver();
        resolver.setAlwaysReload( this.alwaysReload );
        resolver.setComponentName( "jobs." + jobDetail.getFullName() );
        resolver.setRubyClassName( jobDataMap.getString( RUBY_CLASS_NAME_KEY ) );
        resolver.setRubyRequirePath( jobDataMap.getString( RUBY_REQUIRE_PATH_KEY ) );
        */

        try {
            Ruby ruby = this.runtimePool.borrowRuntime();
            IRubyObject rubyObject = resolver.resolve( ruby );
            rubyJob = new RubyJob( this.runtimePool, rubyObject );
        } catch (Exception e) {
            throw new SchedulerException( e );
        }

        return rubyJob;
    }
    
    protected RubyComponentResolver getComponentResolver(String name) {
        KernelRegistryEntry entry = getKernel().getRegistry().getEntry( name );
        if ( entry == null ) {
            return null;
        }
        
        return (RubyComponentResolver) entry.getTarget();
    }

}
