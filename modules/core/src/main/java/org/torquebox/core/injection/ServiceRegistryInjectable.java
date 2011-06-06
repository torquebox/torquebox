package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.torquebox.core.as.CoreServices;

/**
 * Predetermined injectable to provide a <code>ServiceRegistry</code> to the
 * ruby environments.
 * 
 * <p>This injectable provides <code>service-registry</code> as an injectable
 * in all code.  The {@link ServiceRegistry} may be used at runtime to locate
 * MSC {@link Service} instances.
 * 
 * @see ServiceRegistry
 * @see ServiceName
 * @see Service
 * 
 * @author Bob McWhirter
 */
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