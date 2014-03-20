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

package org.torquebox.stomp.processors;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController.Mode;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.stomp.StompletMetaData;
import org.projectodd.stilts.stomplet.container.SimpleStompletContainer;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.StompletService;
import org.torquebox.stomp.as.StompServices;

public class StompletInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        
        List<RubyStompletMetaData> allMetaData = unit.getAttachmentList( RubyStompletMetaData.ATTACHMENTS_KEY );
        
        for ( StompletMetaData each : allMetaData ) {
            deploy( phaseContext, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, StompletMetaData stompletMetaData) {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        StompletService service = new StompletService();
        service.setConfig( stompletMetaData.getStompletConfig() );
        service.setDestinationPattern( stompletMetaData.getDestinationPattern() );
        
        phaseContext.getServiceTarget().addService( StompServices.stomplet( unit, stompletMetaData.getName() ), service )
            .addDependency( StompServices.container( unit ), SimpleStompletContainer.class, service.getStompletContainerInjector() )
            .addDependency( StompServices.stompletComponentResolver( unit, stompletMetaData.getName() ), ComponentResolver.class, service.getComponentResolverInjector() )
            .addDependency( CoreServices.runtimePoolName( unit, "stomplets" ), RubyRuntimePool.class, service.getRuntimePoolInjector() )
            .setInitialMode( Mode.ACTIVE )
            .install();
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( "org.torquebox.stomp" );

}
