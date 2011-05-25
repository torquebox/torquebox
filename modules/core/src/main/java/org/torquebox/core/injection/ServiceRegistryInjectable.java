package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class ServiceRegistryInjectable extends SimpleNamedInjectable {
    
    public ServiceRegistryInjectable() {
        super( "service-registry", "service-registry", false );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws Exception {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        return CoreServices.serviceRegistryName( unit );
    }

    public static final ServiceRegistryInjectable INSTANCE = new ServiceRegistryInjectable();
}