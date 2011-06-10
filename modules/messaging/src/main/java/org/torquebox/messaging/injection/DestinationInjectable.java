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

package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.jndi.JNDIInjectable;

public class DestinationInjectable extends JNDIInjectable {
    
    public DestinationInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext context) {
        ServiceName destinationServiceName = wrapWithManager( context, getDestinationServiceName() );
        ServiceName connectionFactoryServiceName = wrapWithManager( context, getConnectionFactoryServiceName() );
        
        ServiceName liveDestinationServiceName = wrapWithLiveDestination( context, connectionFactoryServiceName, destinationServiceName );
        
        return liveDestinationServiceName;
    }

    protected ServiceName wrapWithLiveDestination(DeploymentPhaseContext context, ServiceName connectionFactoryServiceName, ServiceName destinationServiceName) {
    	ServiceName liveDestinationServiceName = destinationServiceName.append( "live" );
    	
        if (serviceIsAlreadyWrapped( context, liveDestinationServiceName )) {
            return liveDestinationServiceName;
        }
        
        LiveDestinationService liveDestinationService = new LiveDestinationService();
        context.getServiceTarget().addService( liveDestinationServiceName, liveDestinationService )
            .addDependency( connectionFactoryServiceName, ConnectionFactory.class, liveDestinationService.getConnectionFactoryInjector() )
            .addDependency( destinationServiceName, Destination.class, liveDestinationService.getDestinationInjector() )
            .install();
        return liveDestinationServiceName;
    }

    protected ServiceName getDestinationServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }
    
    protected ServiceName getConnectionFactoryServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "ConnectionFactory" );
    }


}
