package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.torquebox.core.as.CoreServices;

/**
 * Predetermined injectable which provides a <code>ServiceTarget</code> for each
 * deployment.
 * 
 * <p>This injectable provides <code>service-target</code> injectable for each 
 * deployment.  The {@link ServiceTarget} may be used to install additional MSC
 * {@link Service} instances at runtime.
 * 
 * @see ServiceTarget
 * @see Service
 * 
 * @author Bob McWhirter
 */
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