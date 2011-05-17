package org.torquebox.messaging.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class MessagingServices {

    private MessagingServices() {
    }

    public static final ServiceName MESSAGING = CoreServices.TORQUEBOX.append( "messaging" );
    public static final ServiceName RUBY_CONNECTION_FACTORY = MESSAGING.append(  "ruby-connection-factory" );

    public static ServiceName messageProcessor(DeploymentUnit unit, String processorName) {
        return unit.getServiceName().append( MESSAGING ).append(  processorName );
    }
    
    public static ServiceName messageProcessorComponentResolver(DeploymentUnit unit, String processorName) {
        return unit.getServiceName().append( MESSAGING ).append(  processorName ).append( "resolver" );
    }
    
    
}
