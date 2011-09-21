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

package org.projectodd.polyglot.web;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.services.path.AbstractPathService;
import org.jboss.as.web.WebServer;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.as.web.WebVirtualHostService;
import org.jboss.msc.service.ServiceName;

public class VirtualHostInstaller implements DeploymentUnitProcessor {
    
    private static final String TEMP_DIR = "jboss.server.temp.dir";

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};
    
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        WebApplicationMetaData webMetaData = unit.getAttachment( WebApplicationMetaData.ATTACHMENT_KEY );
        
        if ( webMetaData == null ) {
            return;
        }
        
        List<String> hosts = new ArrayList<String>();
        hosts.addAll( webMetaData.getHosts() );
        
        if ( hosts.isEmpty() ) {
            return;
        }
        
        String name = hosts.remove( 0 );
        
        ServiceName serviceName = WebSubsystemServices.JBOSS_WEB_HOST.append(name);
        
        if ( phaseContext.getServiceRegistry().getService( serviceName ) != null ) {
            return;
        }
        
        String[] aliases = hosts.toArray( EMPTY_STRING_ARRAY );
        
        WebVirtualHostService service = new WebVirtualHostService( name, aliases, false );
        
        phaseContext.getServiceTarget().addService( serviceName, service )
           .addDependency(AbstractPathService.pathNameOf(TEMP_DIR), String.class, service.getTempPathInjector())
           .addDependency(WebSubsystemServices.JBOSS_WEB, WebServer.class, service.getWebServer())
           .install();
    }
    

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
