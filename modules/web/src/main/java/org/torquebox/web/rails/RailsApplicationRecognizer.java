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

package org.torquebox.web.rails;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.FileLocatingProcessor;

public class RailsApplicationRecognizer extends FileLocatingProcessor {
    
    public static final String DEFAULT_BOOT_RB_PATH = "config/boot.rb";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        
        if (isRailsApplication( root )) {
            RailsApplicationMetaData railsAppMetaData = unit.getAttachment( RailsApplicationMetaData.ATTACHMENT_KEY );
            
            if (railsAppMetaData == null) {
                railsAppMetaData = new RailsApplicationMetaData();
                unit.putAttachment( RailsApplicationMetaData.ATTACHMENT_KEY, railsAppMetaData );
            }
        }

    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }
        
    static boolean isRailsApplication(VirtualFile file) {
        boolean result = hasAnyOf( file, DEFAULT_BOOT_RB_PATH );
        return result;
    }

}
