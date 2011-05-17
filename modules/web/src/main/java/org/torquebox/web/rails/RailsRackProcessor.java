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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.web.rack.RackApplicationMetaData;

public class RailsRackProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        RailsApplicationMetaData railsAppMetaData = unit.getAttachment( RailsApplicationMetaData.ATTACHMENT_KEY );
        
        if (rubyAppMetaData == null || rackAppMetaData == null || railsAppMetaData == null) {
            return;
        }
        
        if (railsAppMetaData.isRails3()) {
            rackAppMetaData.setRackUpScriptLocation( "config.ru" );
        } else {
            rackAppMetaData.setRackUpScript( getRackUpScript( rackAppMetaData.getContextPath() ) );
        }
        
        rackAppMetaData.setStaticPathPrefix( STATIC_PATH_PREFIX );

    }
    
    protected String getRackUpScript(String context) {
        if (context.endsWith( "/" )) {
            context = context.substring( 0, context.length() - 1 );
        }
        return "require %q(org/torquebox/web/rails/rackup)\n" + "run TorqueBox::Rails.app\n";

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }
    
    public static final String STATIC_PATH_PREFIX = "public";

}
