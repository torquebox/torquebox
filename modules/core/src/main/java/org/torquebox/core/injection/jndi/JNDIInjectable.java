package org.torquebox.core.injection.jndi;

import org.jboss.as.ee.naming.ContextNames;
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
    public ServiceName getServiceName() {
        System.err.println( "jndi string [" + getName() + "] ");
        //ServiceName serviceName =  ContextNames.serviceNameOfContext( null, null, null, getName() );
        ServiceName serviceName = ContextNames.JAVA_CONTEXT_SERVICE_NAME.append( getName() );
        System.err.println( "service name[" + serviceName + "] ");
        return serviceName;
    }

}
