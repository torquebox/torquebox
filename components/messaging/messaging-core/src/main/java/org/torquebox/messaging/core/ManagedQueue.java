package org.torquebox.messaging.core;

public class ManagedQueue extends AbstractManagedDestination {
    
    public ManagedQueue() {
        
    }
    
    public void start() throws Exception {
        log.info("Starting queue: "+getName());
        getServer().createQueue(false, getName(), "", false, getName() );
    }
    
    public void destroy() throws Exception {
        log.info("Destroying queue: "+getName());
        getServer().destroyQueue( getName() );
    }

}
