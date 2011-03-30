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

import org.jboss.logging.Logger;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyJobProxy implements Job, StatefulJob {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( RubyJobProxy.class );
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

    private RubyRuntimePool runtimePool;
    private IRubyObject component;

    public RubyJobProxy(RubyRuntimePool runtimePool, IRubyObject component) {
        this.runtimePool = runtimePool;
        this.component = component;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Object jobResult = JavaEmbedUtils.invokeMethod( component.getRuntime(), component, "run", EMPTY_OBJECT_ARRAY, Object.class );
            context.setResult( jobResult );
        } finally {
            this.runtimePool.returnRuntime( component.getRuntime() );
        }
    }

}
