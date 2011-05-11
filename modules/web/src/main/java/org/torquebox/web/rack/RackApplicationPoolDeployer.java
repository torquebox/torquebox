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

package org.torquebox.web.rack;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.torquebox.web.as.WebServices;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: RackApplicationMetaData, RackApplicationPool, PoolMetaData
 * </pre>
 * 
 */
public class RackApplicationPoolDeployer implements DeploymentUnitProcessor {

    static final String POOL_NAME = "web";

    public RackApplicationPoolDeployer() {
    }

    
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if ( ! unit.hasAttachment( RackApplicationMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        String deploymentName = unit.getName();
        
        log.info( "Deploying pool for: " + deploymentName );
        
        RackApplicationPoolImpl pool = new RackApplicationPoolImpl();
        RackApplicationPoolService service = new RackApplicationPoolService( pool );
        
        ServiceName name = WebServices.rackApplicationPoolName( deploymentName );
        ServiceBuilder<RackApplicationPool> builder = phaseContext.getServiceTarget().addService( name, service );
        //builder.addDependency( WebServices.rackApplicationFactoryName( deploymentName ), service.getRackApplicationFactory() );
        builder.addDependency( WebServices.rackApplicationFactoryName( deploymentName ), RackApplicationFactory.class, service.getRackApplicationFactoryInjector() );
        builder.setInitialMode( Mode.ON_DEMAND );
        builder.install();
    }


    @Override
    public void undeploy(DeploymentUnit context) {
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );

}
