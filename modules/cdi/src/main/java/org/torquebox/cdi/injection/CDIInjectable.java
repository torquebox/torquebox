package org.torquebox.cdi.injection;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.weld.WeldContainer;
import org.jboss.as.weld.services.WeldService;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;

public class CDIInjectable extends SimpleNamedInjectable {

    public CDIInjectable(String name, boolean generic) {
        super( "cdi", name, generic );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext phaseContext) throws ClassNotFoundException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceName injectionServiceName = unit.getServiceName().append( "cdi-injection" ).append( getName() );

        if (unit.getServiceRegistry().getService( injectionServiceName ) != null) {
            return injectionServiceName;
        }

        Module module = unit.getAttachment( Attachments.MODULE );
        Class<?> injectionType = module.getClassLoader().loadClass( getName() );

        ServiceName weldServiceName = unit.getServiceName().append( WeldService.SERVICE_NAME );
        CDIInjectableService injectionService = new CDIInjectableService( injectionType );
        phaseContext.getServiceTarget().addService( injectionServiceName, injectionService )
                .addDependency( weldServiceName, WeldContainer.class, injectionService.getWeldContainerInjector() )
                .install();
        return injectionServiceName;
    }

}