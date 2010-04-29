package org.torquebox.jobs.core;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyJobFactory implements JobFactory {

	public static final String RUBY_CLASS_NAME_KEY = "torquebox.ruby.class.name";
	public static final String RUBY_REQUIRE_PATH_KEY = "torquebox.ruby.require.path";

	private RubyRuntimePool runtimePool;
	private JobComponentInitializer componentInitializer;

	public RubyJobFactory() {
		this.componentInitializer = new JobComponentInitializer();
	}

	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}

	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}

	@Override
	public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {
		RubyJob rubyJob = null;

		JobDetail jobDetail = bundle.getJobDetail();
		JobDataMap jobDataMap = jobDetail.getJobDataMap();

		InstantiatingRubyComponentResolver resolver = new InstantiatingRubyComponentResolver();
		resolver.setComponentInitializer(this.componentInitializer);
		resolver.setComponentName("jobs." + jobDetail.getFullName());
		resolver.setRubyClassName(jobDataMap.getString(RUBY_CLASS_NAME_KEY));
		resolver.setRubyRequirePath(jobDataMap.getString(RUBY_REQUIRE_PATH_KEY));

		try {
			Ruby ruby = this.runtimePool.borrowRuntime();
			IRubyObject rubyObject = resolver.resolve(ruby);
			rubyJob = new RubyJob(this.runtimePool, rubyObject);
		} catch (Exception e) {
			throw new SchedulerException(e);
		}

		return rubyJob;
	}

}
