/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyJobHandler implements Job, StatefulJob {
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	public RubyJobHandler() {
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();

		String rubyClassName = jobDataMap.getString(RubyJob.RUBY_CLASS_NAME_KEY);
		String rubyRequirePath = jobDataMap.getString(RubyJob.RUBY_REQUIRE_PATH_KEY);

		InstantiatingRubyComponentResolver resolver = new InstantiatingRubyComponentResolver();
		resolver.setRubyClassName(rubyClassName);
		resolver.setRubyRequirePath( rubyRequirePath );
		resolver.setComponentInitializer( new JobComponentInitializer() );
		
		Ruby ruby = null;

		RubyRuntimePool runtimePool = (RubyRuntimePool) jobDataMap.get(RubyJob.RUNTIME_POOL_KEY);

		try {
			ruby = runtimePool.borrowRuntime();
			IRubyObject rubyJob = resolver.resolve( ruby );
			Object jobResult = JavaEmbedUtils.invokeMethod(ruby, rubyJob, "run", EMPTY_OBJECT_ARRAY, Object.class);
			context.setResult(jobResult);
		} catch (Exception e) {
			context.setResult(e);
			throw new JobExecutionException(e);
		} finally {
			if (ruby != null) {
				runtimePool.returnRuntime(ruby);
			}

		}
	}
}
