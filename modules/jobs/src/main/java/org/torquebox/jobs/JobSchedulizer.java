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
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.projectodd.polyglot.core.AtRuntimeInstaller;
import org.projectodd.polyglot.core.util.ClusterUtil;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.projectodd.polyglot.jobs.BaseJobScheduler;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.processors.ComponentResolverHelper;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.util.StringUtils;
import org.torquebox.jobs.as.JobsServices;
import org.torquebox.jobs.component.JobComponent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.jboss.msc.service.ServiceController.*;


public class JobSchedulizer extends AtRuntimeInstaller<JobSchedulizer> {

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
                    metaData.isSingleton()
            );

            log.debugf("Job '%s' deployed", metaData.getName());
        }
    }

    public ScheduledJob createJob(String rubyClassName, String cronExpression, String timeout, String name, String description, Map<String, Object> config, boolean singleton) {
        TimeInterval timeoutInterval = TimeInterval.parseInterval(timeout, TimeUnit.SECONDS);
        return createJob(rubyClassName, cronExpression, timeoutInterval, name, description, config, singleton);
    }

    public ScheduledJob createJob(String rubyClassName, String cronExpression, TimeInterval timeout, String name, String description, Map<String, Object> config, boolean singleton) {
        log.debugf("Creating new job '%s'...", name);

        String safeName = safeJobName(name);

        ServiceName serviceName = JobsServices.componentResolver(getUnit(), safeName);

        ComponentResolverHelper helper = new ComponentResolverHelper(getTarget(), getUnit(), serviceName);

        try {
            helper
                    .initializeInstantiator(rubyClassName, StringUtils.underscore(rubyClassName.trim()))
                    .initializeResolver(JobComponent.class, config, true) // Always create new instance
                    .installService(Mode.ON_DEMAND);
        } catch (Exception e) {
            log.errorf(e, "Couldn't install '%s' job for deployment unit '%s'", name, getUnit());
        }

        final ScheduledJob job = new ScheduledJob(null, safeName, description, cronExpression, timeout, singleton, rubyClassName);

        installJob(job);

        return job;
    }

    /**
     * Removes the scheduled job by its name.
     * <p/>
     * This operation is executed asynchronously. You cannot expect to have
     * the job undeployed just after the method execution is finished.
     * <p/>
     *
     * @param name Name of the scheduled job
     */
    public void removeJob(String name) {
        removeJob(name, null);
    }

    /**
     * Removes the scheduled job by its name.
     * <p/>
     * This operation is executed asynchronously. You cannot expect to have
     * the job undeployed just after the method execution is finished.
     * <p/>
     * You can use the actionOnRemove parameter to execute code after removal.
     *
     * @param name           Name of the scheduled job
     * @param actionOnRemove Code that should be executed after the job
     *                       removal is complete
     */
    public void removeJob(final String name, final Runnable actionOnRemove) {
        log.debugf("Installing job '%s'", name);

        String safeName = safeJobName(name);

        final ServiceName componentResolverServiceName = JobsServices.componentResolver(getUnit(), safeName);
        final ServiceName jobServiceName = JobsServices.job(getUnit(), safeName);

        replaceService(jobServiceName, new Runnable() {
            @Override
            public void run() {
                replaceService(componentResolverServiceName, actionOnRemove);
            }
        });
    }


    private void installJob(final ScheduledJob job) {
        log.debugf("Installing job '%s'", job.getName());

        final ServiceName serviceName = JobsServices.job(getUnit(), job.getName());

        replaceService(serviceName,
                new Runnable() {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    public void run() {
                        ServiceBuilder builder = build(serviceName, job, job.isSingleton());

                        builder.addDependency(CoreServices.runtimePoolName(getUnit(), "jobs"), RubyRuntimePool.class, job.getRubyRuntimePoolInjector())
                                .addDependency(JobsServices.componentResolver(getUnit(), job.getName()), ComponentResolver.class, job.getComponentResolverInjector())
                                .addDependency(JobsServices.scheduler(getUnit(), job.isSingleton() && ClusterUtil.isClustered(getUnit().getServiceRegistry())), BaseJobScheduler.class, job.getJobSchedulerInjector())
                                .install();

                    }
                });

        installMBean(serviceName, "torquebox.jobs", job);
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

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");
}
