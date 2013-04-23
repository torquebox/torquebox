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

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.projectodd.polyglot.core.AtRuntimeInstaller;
import org.projectodd.polyglot.core.util.ClusterUtil;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.projectodd.polyglot.jobs.BaseJob;
import org.projectodd.polyglot.jobs.BaseJobScheduler;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.processors.ComponentResolverHelper;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.jobs.as.JobsServices;
import org.torquebox.jobs.component.JobComponent;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Service to manage scheduled jobs at runtime.
 *
 * @author Marek Goldmann
 */
public class JobSchedulizer extends AtRuntimeInstaller<JobSchedulizer> {

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");

    public JobSchedulizer(DeploymentUnit unit) {
        super(unit);
    }

    /**
     * When the JobSchedulizer service is starting it deploys all the
     * scheduled jobs defined in deployment descriptors for the current
     * deployment unit.
     *
     * @param context
     * @throws StartException
     */
    @Override
    public void start(StartContext context) throws StartException {
        super.start(context);

        List<ScheduledJobMetaData> allJobMetaData = getUnit().getAttachmentList(ScheduledJobMetaData.ATTACHMENTS_KEY);

        if (allJobMetaData == null || allJobMetaData.size() == 0) {
            return;
        }

        log.debug("Installing jobs listed in deployment descriptors...");

        for (ScheduledJobMetaData metaData : allJobMetaData) {

            log.debugf("Deploying '%s' job...", metaData.getName());

            createJob(
                    metaData.getRubyClassName(),
                    metaData.getCronExpression(),
                    metaData.getTimeout(),
                    metaData.getName(),
                    metaData.getDescription(),
                    metaData.getParameters(),
                    metaData.isSingleton(),
                    metaData.isStopped()
            );

            log.debugf("Job '%s' deployed", metaData.getName());
        }
    }

    /**
     * Creates and deploys a new scheduled job or replace existing.
     * <p/>
     * If there is a job with the same name, it'll be removed and replaced.
     * <p/>
     * Used by the TorqueBox::Jobs::ScheduledJob.schedule method.
     *
     * @param rubyClassName  The Ruby class name of the job implementation
     * @param cronExpression When the job should be executed, in cron format
     * @param timeout        After how much time the job should be interrupted,
     *                       by default it'll never be interrupted. Accepts a
     *                       String value, example: '5s'
     * @param name           Job name
     * @param description    Job description
     * @param config         Job configuration that should be injected to the job
     *                       implementation class constructor
     * @param singleton      Should the job be running only on one node in cluster?
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     * @see JobSchedulizer#createJob(String, String, org.projectodd.polyglot.core.util.TimeInterval, String, String, java.util.Map, boolean, boolean)
     */
    @SuppressWarnings("unused")
    public CountDownLatch createJob(String rubyClassName, String cronExpression, String timeout, String name, String description, Map<String, Object> config, boolean singleton, boolean stopped) {
        TimeInterval timeoutInterval = TimeInterval.parseInterval(timeout, TimeUnit.SECONDS);
        return createJob(rubyClassName, cronExpression, timeoutInterval, name, description, config, singleton, stopped);
    }

    /**
     * Creates and deploys a new scheduled job or replace existing.
     * <p/>
     * If there is a job with the same name, it'll be removed and replaced.
     *
     * @param rubyClassName  The Ruby class name of the job implementation
     * @param cronExpression When the job should be executed, in cron format
     * @param timeout        After how much time the job should be interrupted,
     *                       by default it'll never be interrupted.
     * @param name           Job name
     * @param description    Job description
     * @param config         Job configuration that should be injected to the job
     *                       implementation class constructor
     * @param singleton      Should the job be running only on one node in cluster?
     * @param stopped        Defines if the job should not be started after scheduling
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     */
    public CountDownLatch createJob(final String rubyClassName, String cronExpression, TimeInterval timeout, String name, String description, Map<String, Object> config, boolean singleton, boolean stopped) {
        if (name == null)
            name = safeJobName(rubyClassName);
        else
            name = safeJobName(name);

        log.debugf("Creating new job '%s'...", name);

        ScheduledJob job = new ScheduledJob(getUnit().getName(), name, description, cronExpression, timeout, singleton, stopped, rubyClassName);

        return installJob(job, rubyClassName, job.getComponentResolverInjector(), job.getRubyRuntimePoolInjector(), config);
    }

    /**
     * Creates new 'at' job.
     * <p/>
     * Used by the TorqueBox::Jobs::ScheduledJob.at method.
     *
     * @param rubyClassName The ruby class name of the job implementation
     * @param startAt       The start date of the job
     * @param endAt         The end date of the job
     * @param interval      Interval between job executions
     * @param repeat        How many times the job firing should be repeated (first run is not counted)
     * @param timeout       The job execution timeout
     * @param name          Name of the job
     * @param description   Description of the job
     * @param config        The configuration (if any) which should
     *                      be injected into the job constructor
     * @param singleton     If the job should be a singleton
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     */
    @SuppressWarnings("unused")
    public CountDownLatch createAtJob(String rubyClassName, Date startAt, Date endAt, long interval, int repeat, String timeout, String name, String description, Map<String, Object> config, boolean singleton) {
        TimeInterval timeoutInterval = TimeInterval.parseInterval(timeout, TimeUnit.SECONDS);
        return createAtJob(rubyClassName, startAt, endAt, interval, repeat, timeoutInterval, name, description, config, singleton);
    }

    /**
     * Creates new 'at' job.
     *
     * @param rubyClassName The ruby class name of the job implementation
     * @param startAt       The start date of the job
     * @param endAt         The end date of the job
     * @param interval      Interval between job executions
     * @param repeat        How many times the job firing should be repeated (first run is not counted)
     * @param timeout       The job execution timeout
     * @param name          Name of the job
     * @param description   Description of the job
     * @param config        The configuration (if any) which should
     *                      be injected into the job constructor
     * @param singleton     If the job should be a singleton
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     */
    public CountDownLatch createAtJob(String rubyClassName, Date startAt, Date endAt, long interval, int repeat, TimeInterval timeout, String name, String description, Map<String, Object> config, boolean singleton) {
        if (name == null)
            name = safeJobName(rubyClassName);
        else
            name = safeJobName(name);

        log.debugf("Creating new 'at' job '%s'...", name);

        AtJob atJob = new AtJob(getUnit().getName(), timeout, name, description, singleton);

        atJob.setStartAt(startAt);
        atJob.setEndAt(endAt);
        atJob.setInterval(interval);
        atJob.setRepeat(repeat);

        return installJob(atJob, rubyClassName, atJob.getComponentResolverInjector(), atJob.getRubyRuntimePoolInjector(), config);
    }

    /**
     * Removes the scheduled job by its name.
     * <p/>
     * This operation is executed asynchronously. To watch when the removal is finished use the CountDownLatch returned by this method.
     *
     * @param name Name of the scheduled job
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     */
    @SuppressWarnings("unused")
    public CountDownLatch removeJob(String name) {
        name = safeJobName(name);

        log.debugf("Removing job '%s'", name);

        final ServiceName componentResolverServiceName = JobsServices.componentResolver(getUnit(), name);
        final ServiceName jobServiceName = JobsServices.job(getUnit(), name);
        final CountDownLatch latch = new CountDownLatch(2);

        // Remove the job service
        replaceService(jobServiceName, new Runnable() {
            @Override
            public void run() {
                // Remove the component resolver service once
                // the job service is removed
                replaceService(componentResolverServiceName, new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        return latch;
    }

    /**
     * Installs the component resolver service and the job service for provided job
     *
     * @param job                       The job to install the component resolver for
     * @param componentResolverInjector
     * @param rubyRuntimePoolInjector
     * @param config                    The configuration (if any) which should
     *                                  be injected into the job constructor
     * @return The latch (CountDownLatch) to wait for the task completion.
     * @see CountDownLatch
     */
    private CountDownLatch installJob(
            final BaseJob job,
            final String rubyClassName,
            final Injector<ComponentResolver> componentResolverInjector,
            final Injector<RubyRuntimePool> rubyRuntimePoolInjector,
            final Map<String, Object> config) {

        final ServiceName componentResolverServiceName = JobsServices.componentResolver(getUnit(), job.getName());
        final ServiceName jobServiceName = JobsServices.job(getUnit(), job.getName());
        final CountDownLatch latch = new CountDownLatch(3);

        // First of all remove the job service (if any)
        replaceService(jobServiceName, new Runnable() {
            @Override
            public void run() {
                // Then replace the component resolver
                replaceService(componentResolverServiceName, new Runnable() {
                    @Override
                    public void run() {
                        log.debugf("Installing component resolver for job '%s'...", job.getName());

                        ComponentResolverHelper helper = new ComponentResolverHelper(getTarget(), getUnit(), componentResolverServiceName);

                        try {
                            helper
                                    .initializeInstantiator(rubyClassName, StringUtils.underscore(rubyClassName.trim()))
                                    .initializeResolver(JobComponent.class, config, true) // Always create new instance
                                    .installService(Mode.PASSIVE);
                        } catch (Exception e) {
                            log.errorf(e, "Couldn't install component resolver for job '%s' for deployment unit '%s'", job.getName(), getUnit());
                        }

                        // And install the correct service
                        replaceService(jobServiceName, new Runnable() {
                            @SuppressWarnings({"unchecked", "rawtypes"})
                            public void run() {
                                log.debugf("Installing job '%s'...", job.getName());

                                ServiceBuilder builder = build(jobServiceName, job, job.isSingleton());

                                builder.addDependency(CoreServices.runtimePoolName(getUnit(), "jobs"), RubyRuntimePool.class, rubyRuntimePoolInjector)
                                        .addDependency(JobsServices.componentResolver(getUnit(), job.getName()), ComponentResolver.class, componentResolverInjector)
                                        .addDependency(JobsServices.scheduler(getUnit(), job.isSingleton() && ClusterUtil.isClustered(getUnit().getServiceRegistry())), BaseJobScheduler.class, job.getJobSchedulerInjector())
                                        .install();

                                latch.countDown();

                                installMBean(jobServiceName, "torquebox.jobs", job);
                            }
                        });
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        return latch;
    }

    /**
     * In case of modular job names used in <tt>torquebox.rb</tt> the job name is in
     * format 'Module::JobName'. We need to remove the '::' since it's not
     * allowed in the service name.
     *
     * @param name
     * @return Job name safe to include in the service name
     */
    private String safeJobName(String name) {
        return name.replaceAll("::", ".");
    }
}
