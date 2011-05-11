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

public class RackApplicationDefaultsProcessor implements DeploymentUnitProcessor {

    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final String DEFAULT_STATIC_PATH_PREFIX = "public/";

    public RackApplicationDefaultsProcessor() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        RackApplicationMetaData metadata = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        
        if ( metadata == null ) {
            return;
        }
        
        log.info( "Deploying rack defaults" );
        
        if (metadata.getHosts().isEmpty()) {
            metadata.addHost( DEFAULT_HOST );
        }

        if ((metadata.getContextPath() == null) || (metadata.getContextPath().trim().equals( "" ))) {
            metadata.setContextPath( DEFAULT_CONTEXT_PATH );
        }
        
        if ((metadata.getStaticPathPrefix() == null) || (metadata.getStaticPathPrefix().trim().equals( "" ))) {
            metadata.setStaticPathPrefix( DEFAULT_STATIC_PATH_PREFIX );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack" );
}
