package org.torquebox.messaging.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.messaging.as.MessagingServices;

public class ConnectionFactoryInjectable extends SimpleNamedInjectable {
    
    public ConnectionFactoryInjectable() {
        super( "connection-factory", "connection-factory", false );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws Exception {
        return MessagingServices.RUBY_CONNECTION_FACTORY;
    }
    
    public static final ConnectionFactoryInjectable INSTANCE = new ConnectionFactoryInjectable();
}