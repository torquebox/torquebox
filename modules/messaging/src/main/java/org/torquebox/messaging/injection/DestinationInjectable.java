package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.jndi.JNDIInjectable;

public class DestinationInjectable extends JNDIInjectable {
    
    public DestinationInjectable(String name, boolean generic) {
        super( name, generic );
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
