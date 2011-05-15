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

package org.torquebox.services.component;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.component.ComponentResolverService;
import org.torquebox.services.ServiceMetaData;
import org.torquebox.services.as.ServicesServices;

public class ServicesComponentResolverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        List<ServiceMetaData> allServiceMetaData = unit.getAttachmentList( ServiceMetaData.ATTACHMENTS_KEY );
        
        for (ServiceMetaData serviceMetaData : allServiceMetaData) {
            deploy( phaseContext, serviceMetaData );
        }
    }
    
    protected void deploy(DeploymentPhaseContext phaseContext, ServiceMetaData serviceMetaData) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        ComponentClass instantiator = new ComponentClass();
        instantiator.setClassName( serviceMetaData.getClassName() );
        
        ServiceName serviceName = ServicesServices.serviceComponentResolver( unit, serviceMetaData.getClassName() );
        ComponentResolver resolver = new ComponentResolver();
        resolver.setComponentInstantiator( instantiator );
        resolver.setComponentName( serviceName.getCanonicalName() );
        resolver.setComponentWrapperClass( ServicesComponent.class );
        resolver.setInitializeParams( serviceMetaData.getParameters() );
        
        log.info( "Installing Services component resolver: " + serviceName );
        ComponentResolverService service = new ComponentResolverService( resolver );
        ServiceBuilder<ComponentResolver> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.setInitialMode( Mode.PASSIVE );
        builder.install();
    }

    @Override
    public void undeploy(DeploymentUnit unit) {

    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.services.component" );

}
