package org.torquebox.core.injection.msc;

import org.jboss.msc.service.ServiceName;
import org.torquebox.core.injection.SimpleNamedInjectable;


public class ServiceInjectable extends SimpleNamedInjectable {
    
    public ServiceInjectable(String name, boolean generic) {
        this( "jndi", name, generic );
    }
    
    protected ServiceInjectable(String type, String name, boolean generic) {
        super( type, name, generic );
    }

    @Override
    public ServiceName getServiceName() {
        return ServiceName.parse( getName() );
    }

}
