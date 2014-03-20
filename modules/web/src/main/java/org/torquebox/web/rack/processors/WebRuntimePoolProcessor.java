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

package org.torquebox.web.rack.processors;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceRegistry;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.web.as.HttpConnectorStartService;
import org.torquebox.web.as.WebServices;
import org.torquebox.web.rack.RackMetaData;

public class WebRuntimePoolProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        if ( ! unit.hasAttachment( RackMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        List<PoolMetaData> allMetaData = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        PoolMetaData poolMetaData = PoolMetaData.extractNamedMetaData( allMetaData, "web" );
        
        if ( poolMetaData == null ) {
            poolMetaData = new PoolMetaData("web");
            poolMetaData.setShared();
            poolMetaData.setDeferUntilRequested( false );
            unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        }
        
        String forceConnectorStart = System.getProperty( "org.torquebox.web.force_http_connector_start", "false" );
        if (Boolean.parseBoolean( forceConnectorStart )) {
            HttpConnectorStartService webStartService = new HttpConnectorStartService();
            phaseContext.getServiceTarget().addService( WebServices.WEB_CONNECTOR_START, webStartService )
                    .addDependency( CoreServices.runtimeStartPoolName( unit, "web" ) )
                    .addDependency( CoreServices.serviceRegistryName( unit ), ServiceRegistry.class, webStartService.getServiceRegistryInjector() )
                    .install();
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
