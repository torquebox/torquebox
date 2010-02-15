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
package org.torquebox.ruby.enterprise.jobs;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.torquebox.common.util.StringUtils;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class RubyJobHandler implements Job, StatefulJob {
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	public RubyJobHandler() {
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();

		String rubyClassName = (String) jobDataMap.get(RubyJob.RUBY_CLASS_NAME_KEY);
		RubyRuntimePool runtimePool = (RubyRuntimePool) jobDataMap.get(RubyJob.RUNTIME_POOL_KEY);

		Ruby ruby = null;

		try {
			ruby = runtimePool.borrowRuntime();
			
			loadSupport(ruby);
			
			String requirePath = StringUtils.underscore(rubyClassName).replaceAll("::", "/");
			String require = "load %q(" + requirePath + ".rb)";

			ruby.evalScriptlet(require);

			RubyModule rubyClass = ruby.getClassFromPath(rubyClassName);

			IRubyObject rubyJob = (IRubyObject) JavaEmbedUtils.invokeMethod(ruby, rubyClass, "new", EMPTY_OBJECT_ARRAY, Object.class);

			injectLogger(rubyJob, rubyClassName);
			
			JavaEmbedUtils.invokeMethod( ruby, rubyJob, "run", EMPTY_OBJECT_ARRAY, void.class );

		} catch (Exception e) {
			throw new JobExecutionException(e);
		} finally {
			if (ruby != null) {
				runtimePool.returnRuntime(ruby);
			}

		}
	}

	protected void loadSupport(Ruby runtime) {
		String supportScript = "require %q(torquebox/jobs/base)\n";
		runtime.evalScriptlet(supportScript);
	}

	private void injectLogger(IRubyObject rubyJob, String rubyClassName) {
		boolean isInjectable = ((Boolean) JavaEmbedUtils.invokeMethod(rubyJob.getRuntime(), rubyJob, "respond_to?",
				new Object[] { "log=" }, Boolean.class)).booleanValue();

		if (isInjectable) {
			String loggerName = rubyClassName.replaceAll("::", ".");
			Logger logger = Logger.getLogger(loggerName);
			//log.info( "injecting " + logger );
			JavaEmbedUtils.invokeMethod(rubyJob.getRuntime(), rubyJob, "log=", new Object[] { logger }, void.class);
		} else {
			//log.warn("Unable to inject log into " + rubyClassName);
		}
	}

}
