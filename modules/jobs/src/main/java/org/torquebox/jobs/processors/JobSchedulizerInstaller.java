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
import org.jboss.msc.service.ServiceController.Mode;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.jobs.JobSchedulizer;
import org.torquebox.jobs.as.JobsServices;

/**
 * Creates a JobSchedulizer service
 * <p/>
 * This service is used to schedule jobs at runtime.
 */
public class JobSchedulizerInstaller implements DeploymentUnitProcessor {
    @Override
    public void deploy(DeploymentPhaseContext context) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = context.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        if (!unit.hasAttachment(RubyAppMetaData.ATTACHMENT_KEY)) {
            return;
        }

        JobSchedulizer service = new JobSchedulizer(unit);

        log.debugf("Deploying job schedulizer for deployment unit '%s'", unit.getName());

        context.getServiceTarget().addService(JobsServices.schedulizer(unit), service)
                .setInitialMode(Mode.ACTIVE)
                .install();
    }

    @Override
    public void undeploy(DeploymentUnit unit) {
    }

    private static final Logger log = Logger.getLogger("org.torquebox.jobs");
}
