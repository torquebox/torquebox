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

package org.torquebox.jobs.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.util.ClusterUtil;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.hasingleton.HASingleton;
import org.projectodd.polyglot.jobs.BaseJobScheduler;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.jobs.JobScheduler;
import org.torquebox.jobs.JobSchedulerMetaData;
import org.torquebox.jobs.as.JobsServices;

/**
 * Creates a JobScheduler service(s)
 *
 * Creates always a local job scheduler for selected deployment unit.
 *
 * If we're in a clustered environment an additional singleton scheduler
 * will be created.
 */
public class JobSchedulerInstaller implements DeploymentUnitProcessor {

    public JobSchedulerInstaller() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        RubyAppMetaData rubyAppMetaData = unit.getAttachment(RubyAppMetaData.ATTACHMENT_KEY);

        if (rubyAppMetaData == null) {
            return;
        }

        if (ClusterUtil.isClustered(phaseContext)) {
            log.debugf("Deploying clustered scheduler for deployment unit '%s'", unit.getName());
            installScheduler(phaseContext, true);
        }

        log.debugf("Deploying regular scheduler for deployment unit '%s'", unit.getName());
        installScheduler(phaseContext, false);
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    private void installScheduler(DeploymentPhaseContext phaseContext, boolean singleton) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ServiceName serviceName = JobsServices.scheduler(unit, singleton);
        JobSchedulerMetaData schedulerMetaData = unit.getAttachment(JobSchedulerMetaData.ATTACHMENT_KEY);

        if (schedulerMetaData == null) {
            // It seems that there are no jobs defined at the deployment time
            // (in deployment descriptors) or no jobs subsystem configuration
            // is provided, let's create a default one.
            log.debug("No jobs subsystem configuration is found, using defaults");

            schedulerMetaData = new JobSchedulerMetaData();
            unit.putAttachment(JobSchedulerMetaData.ATTACHMENT_KEY, schedulerMetaData);
        }

        log.debugf("Setting job scheduler concurrency to %s", schedulerMetaData.getThreadCount());

        JobScheduler scheduler = new JobScheduler(serviceName.getCanonicalName(), schedulerMetaData.getThreadCount());

        ServiceBuilder<BaseJobScheduler> builder = phaseContext.getServiceTarget().addService(serviceName, scheduler);

        // Add dependency to the jobs pool
        builder.addDependency(CoreServices.runtimePoolName(unit, "jobs"), RubyRuntimePool.class, scheduler.getRubyRuntimePoolInjector());

        if (singleton) {
            // A singleton scheduler needs to depend on a HA Singleton
            builder.addDependency(HASingleton.serviceName(unit, "global"));
            // Come up only when the deps are satisfied (in this case HA Singleton)
            builder.setInitialMode(Mode.PASSIVE);
        } else {
            // Start, start, start!
            builder.setInitialMode(Mode.ACTIVE);
        }

        builder.install();
    }

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");
}
