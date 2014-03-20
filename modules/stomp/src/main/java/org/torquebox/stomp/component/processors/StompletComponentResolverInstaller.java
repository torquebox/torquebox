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

package org.torquebox.stomp.component.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.component.processors.ComponentResolverHelper;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.as.StompServices;
import org.torquebox.stomp.component.XAStompletComponent;

import java.util.List;

import static org.jboss.msc.service.ServiceController.Mode;

public class StompletComponentResolverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        List<RubyStompletMetaData> allMetaData = unit.getAttachmentList(RubyStompletMetaData.ATTACHMENTS_KEY);

        for (RubyStompletMetaData each : allMetaData) {
            deploy(phaseContext, each);
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, RubyStompletMetaData metaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceName serviceName = StompServices.stompletComponentResolver(unit, metaData.getName());

        ComponentResolverHelper helper = new ComponentResolverHelper(phaseContext, serviceName);

        try {
            helper
                    .initializeInstantiator(metaData.getRubyClassName(), metaData.getRubyRequirePath())
                    .initializeResolver(XAStompletComponent.class, null, false)
                    .installService(Mode.ON_DEMAND);
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("org.torquebox.jobs.component");

}
