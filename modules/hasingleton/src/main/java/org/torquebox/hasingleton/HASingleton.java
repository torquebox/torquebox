package org.torquebox.hasingleton;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class HASingleton implements Service<Void> {
    
    public static ServiceName serviceName(DeploymentUnit unit) {
        return unit.getServiceName().append(  "ha-singleton"  );
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info( "HA Singleton starting" );
        
    }

    @Override
    public void stop(StopContext context) {
        log.info( "HA Singleton stopping" );
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.hasingleton"  );

}
