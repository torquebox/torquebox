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

package org.torquebox.core.runtime.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceController;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.RuntimeRestartScanner;

public class RuntimeRestartProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        final RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            return;
        }
        RuntimeRestartScanner scanner = getRuntimeRestartScanner( unit );
        scanner.addDeploymentUnit( unit );
    }

    @Override
    public void undeploy(DeploymentUnit unit) {
        RuntimeRestartScanner scanner = getRuntimeRestartScanner( unit );
        // During shutdown scanner may be removed before undeploy
        if (scanner != null) {
            scanner.removeDeploymentUnit( unit );
        }
    }

    @SuppressWarnings("rawtypes")
    protected RuntimeRestartScanner getRuntimeRestartScanner(DeploymentUnit unit) {
        ServiceController serviceController = unit.getServiceRegistry().
                getService( CoreServices.RUNTIME_RESTART_SCANNER );
        if (serviceController != null) {
            return (RuntimeRestartScanner) serviceController.getValue();
        }
        return null;
    }

}
