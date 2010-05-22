package org.torquebox.messaging.core;


public class ManagedQueue extends AbstractManagedDestination {
	
	public ManagedQueue() {
		
	}
	
	public void start() throws Exception {
		log.info( "start queue " + getName() );
		
		getServer().createQueue(false, getName(), "", false, getName() );
	}
	
	public void destroy() throws Exception {
		log.info( "destroy queue " + getName() );
		
		getServer().destroyQueue( getName() );
	}

}
