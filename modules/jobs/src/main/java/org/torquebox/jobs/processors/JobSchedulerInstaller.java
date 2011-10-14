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

package org.torquebox.jobs.processors;

import java.awt.image.Kernel;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.processors.ClusterAwareProcessor;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.hasingleton.HASingleton;
import org.torquebox.jobs.JobScheduler;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;

/**
 * Creates a JobScheduler service if there are any job meta data
 */
public class JobSchedulerInstaller extends ClusterAwareProcessor {

    public JobSchedulerInstaller() {
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public Kernel getKernel() {
        return this.kernel;
    }

    public void setRubyRuntimePoolName(String runtimePoolName) {
        this.runtimePoolName = runtimePoolName;
    }

    public String getRubyRuntimePoolName() {
        return this.runtimePoolName;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<ScheduledJobMetaData> allJobMetaData =
                unit.getAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY );

        if (!allJobMetaData.isEmpty()) {
            deploy( phaseContext, allJobMetaData );
        }
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    public void deploy(DeploymentPhaseContext phaseContext, List<ScheduledJobMetaData> allMetaData) throws DeploymentUnitProcessingException {
        DeployedJobTypes jobTypes = getJobTypes( allMetaData );
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (isClustered( phaseContext )) {
            log.debug( "Deploying clustered scheduler: " + unit );
            if (jobTypes.singletonJobs) {
                this.buildScheduler( phaseContext, true );
            }

            if (jobTypes.regularJobs) {
                this.buildScheduler( phaseContext, false );
            }
        } else {
            log.debug( "Deploying scheduler: " + unit );
            this.buildScheduler( phaseContext, false );
        }
    }

    private void buildScheduler(DeploymentPhaseContext phaseContext, boolean singleton) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ServiceName serviceName = JobsServices.jobScheduler( unit, singleton );

        log.info( "Installing Job Scheduler: " + serviceName );

        JobScheduler scheduler = new JobScheduler( "JobScheduler$" + unit.getName() );

        ServiceBuilder<JobScheduler> builder = phaseContext.getServiceTarget().addService( serviceName, scheduler );
        builder.addDependency( CoreServices.runtimePoolName( unit, "jobs" ), RubyRuntimePool.class, scheduler.getRubyRuntimePoolInjector() );

        if (singleton) {
            builder.addDependency( HASingleton.serviceName( unit ) );
            builder.setInitialMode( Mode.PASSIVE );
        } else {
            builder.setInitialMode( Mode.ACTIVE );
        }

        builder.install();
    }

    private DeployedJobTypes getJobTypes(List<ScheduledJobMetaData> allMetaData) {
        DeployedJobTypes deployedJobTypes = new DeployedJobTypes();
        for (ScheduledJobMetaData each : allMetaData) {
            if (each.isSingleton()) {
                deployedJobTypes.singletonJobs = true;
            }
            else {
                deployedJobTypes.regularJobs = true;
            }
        }
        return deployedJobTypes;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.jobs" );

    private String runtimePoolName;
    private Kernel kernel;

    private class DeployedJobTypes {
        boolean regularJobs = false;
        boolean singletonJobs = false;
    }

}
