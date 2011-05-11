package org.torquebox.web.rack;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class RackApplicationFactoryService implements Service<RackApplicationFactory> {

    public RackApplicationFactoryService(RackApplicationFactoryImpl factory) {
        this.factory = factory;
    }
    
    @Override
    public RackApplicationFactory getValue() throws IllegalStateException, IllegalArgumentException {
        return this.factory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info( "Starting" );
        // nothing
    }

    @Override
    public void stop(StopContext context) {
        log.info( "Stopping" );
    }
    
    private RackApplicationFactoryImpl factory;

    private static final Logger log = Logger.getLogger( "org.torquebox.web.rack.factory" );
}
