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

package org.torquebox.services.component.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.as.DeploymentNotifier;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.component.processors.ComponentResolverHelper;
import org.torquebox.services.ServiceMetaData;
import org.torquebox.services.as.ServicesServices;
import org.torquebox.services.component.ServiceComponent;
import org.torquebox.services.injection.ServiceInjectable;

import java.util.Arrays;
import java.util.List;

import static org.jboss.msc.service.ServiceController.*;

public class ServiceComponentResolverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        List<ServiceMetaData> allServiceMetaData = unit.getAttachmentList(ServiceMetaData.ATTACHMENTS_KEY);

        for (ServiceMetaData serviceMetaData : allServiceMetaData) {
            deploy(phaseContext, serviceMetaData);
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, ServiceMetaData metaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceName serviceName = ServicesServices.serviceComponentResolver(unit, metaData.getName());

        ComponentResolverHelper helper = new ComponentResolverHelper(phaseContext, serviceName);

        try {
            helper
                    .initializeInstantiator(metaData.getClassName(), metaData.getRubyRequirePath())
                    .initializeResolver(ServiceComponent.class, metaData.getParameters(), false, false)
                    .installService(Mode.PASSIVE);
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("org.torquebox.services.component");

}
