package org.torquebox.services.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.services.as.ServicesServices;

public class ServiceInjectable extends SimpleNamedInjectable {

    public ServiceInjectable(String name) {
        super( "service", name, false );
    }
    
    public String getKey() {
        return "service:" + getName();
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws Exception {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ServiceName serviceName = ServicesServices.serviceInjectableService( unit, getName() );
        return serviceName;
    }
    
}