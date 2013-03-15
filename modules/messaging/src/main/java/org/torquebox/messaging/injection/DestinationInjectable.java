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

package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.projectodd.polyglot.messaging.destinations.DestinationUtils;
import org.torquebox.core.injection.jndi.JNDIInjectable;

public class DestinationInjectable extends JNDIInjectable {

    public DestinationInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) {
        ServiceName destinationServiceName = wrapWithManager( serviceTarget, unit, getDestinationServiceName() );
        ServiceName connectionFactoryServiceName = wrapWithManager( serviceTarget, unit, getConnectionFactoryServiceName() );

        ServiceName liveDestinationServiceName = wrapWithLiveDestination( serviceTarget, unit, connectionFactoryServiceName, destinationServiceName );

        return liveDestinationServiceName;
    }

    protected ServiceName wrapWithLiveDestination(ServiceTarget serviceTarget, DeploymentUnit unit, ServiceName connectionFactoryServiceName,
            ServiceName destinationServiceName) {
        ServiceName liveDestinationServiceName = destinationServiceName.append( "live" );

        synchronized(unit.getServiceRegistry()) {
            if (serviceIsAlreadyWrapped( unit, liveDestinationServiceName )) {
                return liveDestinationServiceName;
            }

            LiveDestinationService liveDestinationService = new LiveDestinationService();
            serviceTarget.addService( liveDestinationServiceName, liveDestinationService )
            .addDependency( connectionFactoryServiceName, ConnectionFactory.class, liveDestinationService.getConnectionFactoryInjector() )
            .addDependency( destinationServiceName, Destination.class, liveDestinationService.getDestinationInjector() )
            .addDependency( DependencyType.OPTIONAL, DestinationUtils.destinationPointerName(unit, getName()) )
            .install();
        }
        return liveDestinationServiceName;
    }

    protected ServiceName getDestinationServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( DestinationUtils.getServiceName( getName() ) );
    }

    protected ServiceName getConnectionFactoryServiceName() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( "ConnectionFactory" );
    }

}
