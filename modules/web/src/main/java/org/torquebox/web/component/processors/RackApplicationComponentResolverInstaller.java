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

package org.torquebox.web.component.processors;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.core.component.ComponentEval;
import org.torquebox.core.component.processors.ComponentResolverHelper;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.component.RackApplicationComponent;
import org.torquebox.web.rack.RackMetaData;

public class RackApplicationComponentResolverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }

        ServiceName serviceName = WebServices.rackApplicationComponentResolver(unit);
        RubyAppMetaData rubyAppMetaData = unit.getAttachment(RubyAppMetaData.ATTACHMENT_KEY);
        RackMetaData rackAppMetaData = unit.getAttachment(RackMetaData.ATTACHMENT_KEY);

        if (rubyAppMetaData == null || rackAppMetaData == null) {
            return;
        }

        ResourceRoot resourceRoot = unit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        VirtualFile root = resourceRoot.getRoot();

        ComponentEval instantiator = new ComponentEval();

        try {
            String rackUpFile = rackAppMetaData.getRackUpScriptFile(root.getPhysicalFile()).getAbsolutePath();
            instantiator.setCode(getCode(rackAppMetaData.getRackUpScript(root.getPhysicalFile()), rackUpFile));
            instantiator.setLocation(rackUpFile);

            ComponentResolverHelper helper = new ComponentResolverHelper(phaseContext, serviceName);

            helper
                    .initializeInstantiator(instantiator)
                    .initializeResolver(RackApplicationComponent.class, null, false, false) // Let Rack / Rails handle reloading for the web stack
                    .installService(Mode.ON_DEMAND);

        } catch (Exception e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    protected String getCode(String rackUpScript, String rackUpFile) {
        StringBuilder code = new StringBuilder();
        code.append("require %q(rack)\n");
        if (rackUpScript != null) {
            code.append("rackUpScript = %q(" + rackUpScript + ")\n");
        } else {
            code.append("rackUpScript = File.read(%q(" + rackUpFile + "))\n");
        }
        code.append("eval(%Q(Rack::Builder.new{\n");
        code.append("#{rackUpScript}");
        code.append("\n}.to_app), TOPLEVEL_BINDING, %q(");
        code.append(rackUpFile);
        code.append("), 0)");
        return code.toString();
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("org.torquebox.web.component");
}
