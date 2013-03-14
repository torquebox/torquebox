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
import org.torquebox.jobs.JobSchedulizer;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;

import java.util.List;

/**
 * <pre>
 * Stage: REAL
 *    In: ScheduledJobMetaData
 *   Out: ScheduledJob
 * </pre>
 * <p/>
 * Creates objects from metadata
 */
public class ScheduledJobInstaller implements DeploymentUnitProcessor {

    public ScheduledJobInstaller() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<ScheduledJobMetaData> allJobMetaData = unit.getAttachmentList(ScheduledJobMetaData.ATTACHMENTS_KEY);

        if (!allJobMetaData.isEmpty()) {

            JobSchedulizer jobSchedulizer;

            try {
                jobSchedulizer = (JobSchedulizer) phaseContext.getServiceRegistry().getService(JobsServices.schedulizer(phaseContext.getDeploymentUnit())).getService().getValue();
            } catch (Exception e) {
                log.errorf(e, "Could not obtain JobSchedulizer for %s deployment unit. Deploying jobs from deployment descriptors failed", phaseContext.getDeploymentUnit().getName());
                return;
            }

            for (ScheduledJobMetaData metaData : allJobMetaData) {

                log.debugf("Deploying '%s' job...", metaData.getName());

                jobSchedulizer.createJob(
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
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");
}
