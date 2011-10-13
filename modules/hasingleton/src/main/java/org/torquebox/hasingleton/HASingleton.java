package org.torquebox.hasingleton;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class HASingleton implements Service<Void> {
    
    public static ServiceName serviceName() {
        return ServiceName.of(  "torquebox", "ha", "singleton"  );
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(StartContext context) throws StartException {
        
    }

    @Override
    public void stop(StopContext context) {
        
    }

}
