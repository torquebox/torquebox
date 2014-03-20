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

package org.torquebox.web.rails;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.web.rack.RackMetaData;

import java.io.File;

public class RailsRackProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        RubyAppMetaData rubyAppMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );
        RailsMetaData railsAppMetaData = unit.getAttachment( RailsMetaData.ATTACHMENT_KEY );
        
        if (rubyAppMetaData == null || rackAppMetaData == null || railsAppMetaData == null) {
            return;
        }
        
        if (!railsAppMetaData.isRails3()) {
            File root = rubyAppMetaData.getRoot();
            File rackup = rackAppMetaData.getRackUpScriptFile(root);
            if (!rackup.exists()) {
                rackAppMetaData.setRackUpScript( getRackUpScript( rackAppMetaData.getContextPath() ) );
            }
        }
    }
    
    protected String getRackUpScript(String context) {
        if (context != null && context.endsWith( "/" )) {
            context = context.substring( 0, context.length() - 1 );
        }
        return "require %q(org/torquebox/web/rails/rackup)\n" + "run TorqueBox::Rails.app\n";

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }
}
