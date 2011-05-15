package org.torquebox.core.injection.jndi;

import org.jboss.as.ee.naming.ContextNames;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;

public class JNDIInjectable extends SimpleNamedInjectable {

    public JNDIInjectable(String name, boolean generic) {
        this( "jndi", name, generic );
    }

    protected JNDIInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName(DeploymentPhaseContext context) {
        return wrapWithManager( context, getServiceNameInternal() );
    }

    protected ServiceName getServiceNameInternal() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }

    protected ServiceName wrapWithManager(DeploymentPhaseContext context, ServiceName serviceName) {
        ServiceName managementServiceName = serviceName.append( "manager" );

        ManagedReferenceInjectableService managementService = new ManagedReferenceInjectableService();
        context.getServiceTarget().addService( managementServiceName, managementService )
            .addDependency( serviceName, ManagedReferenceFactory.class, managementService.getManagedReferenceFactoryInjector() )
            .install();

        return managementServiceName;

    }

}
