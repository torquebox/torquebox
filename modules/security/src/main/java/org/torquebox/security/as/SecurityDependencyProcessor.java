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

package org.torquebox.security.as;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.projectodd.polyglot.core.util.DeploymentUtils;

public class SecurityDependencyProcessor implements DeploymentUnitProcessor {

    private static ModuleIdentifier TORQUEBOX_SECURITY_ID = ModuleIdentifier.create("org.torquebox.security");
    
    @Override
    /** {@inheritDoc} */
    public void deploy(DeploymentPhaseContext phaseContext)
            throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( deploymentUnit )) {
            return;
        }
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpecification.addLocalDependency( new ModuleDependency( moduleLoader,
                TORQUEBOX_SECURITY_ID, false, true, false, false ) );
    }

    @Override
    /** {@inheritDoc} */
    public void undeploy(DeploymentUnit unit) {
    }

}
