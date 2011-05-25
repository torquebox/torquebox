package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class ServiceTargetInjectable extends SimpleNamedInjectable {
    
    public ServiceTargetInjectable() {
        super( "service-target", "service-target", false );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws Exception {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        return CoreServices.serviceTargetName( unit );
    }

    public static final ServiceTargetInjectable INSTANCE = new ServiceTargetInjectable();
}