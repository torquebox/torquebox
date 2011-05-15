package org.torquebox.messaging.injection;

import org.jboss.as.ee.naming.ContextNames;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.ConvertibleInjectableService;
import org.torquebox.core.injection.jndi.JNDIInjectable;

public class QueueInjectable extends JNDIInjectable {

    public QueueInjectable(String name, boolean generic) {
        this( "queue", name, generic );
    }

    protected QueueInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }
    
    @Override
    public ServiceName getServiceName(DeploymentPhaseContext context) {
        ServiceName serviceName = super.getServiceName( context );
        
        return wrapWithConverter( context, serviceName );
    }

    protected ServiceName wrapWithConverter(DeploymentPhaseContext context, ServiceName serviceName) {
        ServiceName converterServiceName = serviceName.append( "converter" );
        QueueConverter converter = new QueueConverter();
        ConvertibleInjectableService converterService = new ConvertibleInjectableService( converter );
        context.getServiceTarget().addService( converterServiceName, converterService )
            .addDependency( serviceName, converterService.getObjectInjector() )
            .install();
        return converterServiceName;
    }
    
    @Override
    protected ServiceName getServiceNameInternal() {
        return ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
    }

}
