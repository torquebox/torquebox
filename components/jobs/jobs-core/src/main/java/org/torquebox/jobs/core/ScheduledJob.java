/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.text.ParseException;

import org.jboss.logging.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.torquebox.interp.spi.RubyRuntimePool;

public class ScheduledJob {

	private static final Logger log = Logger.getLogger(ScheduledJob.class);

	public static final String RUNTIME_POOL_KEY = "torquebox.ruby.pool";

	private String group;
	private String name;
	private String description;

	private String rubyClassName;
	private String rubyRequirePath;

	private String cronExpression;

	private RubyRuntimePool runtimePool;
	private Scheduler scheduler;

	public ScheduledJob() {

	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return this.group;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}

	public String getRubyClassName() {
		return this.rubyClassName;
	}

	public void setRubyRequirePath(String rubyRequirePath) {
		this.rubyRequirePath = rubyRequirePath;
	}

	public String getRubyRequirePath() {
		return this.rubyRequirePath;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpression() {
		return this.cronExpression;
	}

	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}

	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public Scheduler getScheduler() {
		return this.scheduler;
	}

	public void start() throws ParseException, SchedulerException {
		log.info("Starting Ruby job: " + this.group + "." + this.name);
		JobDetail jobDetail = new JobDetail();

		jobDetail.setGroup(this.group);
		jobDetail.setName(this.name);
		jobDetail.setDescription(this.description);
		jobDetail.setJobClass(RubyJob.class);

		JobDataMap jobData = jobDetail.getJobDataMap();

		jobData.put(RubyJobFactory.RUBY_CLASS_NAME_KEY, this.rubyClassName);
		if ( (this.rubyRequirePath != null) && (!this.rubyRequirePath.trim().equals( "" )) ) {
			jobData.put(RubyJobFactory.RUBY_REQUIRE_PATH_KEY, this.rubyRequirePath);
		}

		CronTrigger trigger = new CronTrigger(getTriggerName(), this.group, this.cronExpression);
		scheduler.scheduleJob(jobDetail, trigger);
	}

	private String getTriggerName() {
		return this.name + ".trigger";
	}

	public void stop() throws SchedulerException {
		log.info("Stopping Ruby job: " + this.group + "." + this.name);
		scheduler.unscheduleJob(getTriggerName(), this.group);
	}

	public String toString() {
		return "[RubyJob: name=" + this.name + "; description=" + this.description + "; rubyClass=" + this.rubyClassName + "]";
	}

}
