package org.torquebox.messaging.core;

public class ManagedTopic extends AbstractManagedDestination {

    public ManagedTopic() {

    }

    public void start() throws Exception {
        log.info( "Starting topic " + getName() );
        getServer().createTopic( false, getName(), getName() );
    }

    public void destroy() throws Exception {
        log.info( "Destroying topic " + getName() );
        getServer().destroyTopic( getName() );
    }

}
