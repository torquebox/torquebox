package org.torquebox.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.ServiceRegistry;

public class MockServiceRegistry implements ServiceRegistry {

    @Override
    public ServiceController<?> getRequiredService(ServiceName serviceName) throws ServiceNotFoundException {
        ServiceController<?> service = registry.get( serviceName );
        if ( service == null ) {
            throw new ServiceNotFoundException();
        }
        
        return service;
    }

    @Override
    public ServiceController<?> getService(ServiceName serviceName) {
        return registry.get(  serviceName  );
    }

    @Override
    public List<ServiceName> getServiceNames() {
        List<ServiceName> names = new ArrayList<ServiceName>();
        names.addAll( registry.keySet() );
        return names;
    }
    
    private Map<ServiceName,ServiceController<?>> registry = new HashMap<ServiceName,ServiceController<?>>();

}
