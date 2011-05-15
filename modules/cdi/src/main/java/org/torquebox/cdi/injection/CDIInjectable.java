package org.torquebox.cdi.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;

public class CDIInjectable extends SimpleNamedInjectable {

    public CDIInjectable(String name, boolean generic) {
        super( "cdi", name, generic );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) {
        return null;
    }
    
}