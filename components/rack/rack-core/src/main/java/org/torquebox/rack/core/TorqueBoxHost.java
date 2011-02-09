package org.torquebox.rack.core;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.jboss.logging.Logger;

public class TorqueBoxHost extends StandardHost {
    
    private static final Logger log = Logger.getLogger( TorqueBoxHost.class );

    @Override
    public void addChild(Container child) {
        log.info( "addChild(" + child + ") BEGIN" );
        super.addChild( child );
        log.info( "addChild(" + child + ") END" );
    }

    @Override
    public synchronized void start() throws LifecycleException {
        log.info( "start() BEGIN" );
        super.start();
        log.info( "start() END" );
    }

    @Override
    public synchronized void init() {
        log.info( "init() BEGIN" );
        super.init();
        log.info( "init() END" );
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName oname) throws Exception {
        log.info( "preRegister(" + oname + ") BEGIN" );
        ObjectName result = super.preRegister( server, oname );
        log.info( "preRegister(" + oname + ") END " + result );
        return result;
    }

    @Override
    public synchronized void stop() throws LifecycleException {
        log.info( "stop() BEGIN" );
        super.stop();
        log.info( "stop() END" );
    }

}
