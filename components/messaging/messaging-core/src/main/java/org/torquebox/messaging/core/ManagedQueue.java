package org.torquebox.messaging.core;


public class ManagedQueue extends AbstractManagedDestination {
	
	public ManagedQueue() {
		
	}
	
	public void start() throws Exception {
		System.err.println( "start queue " + getName() );
		
		getServer().createQueue(false, getName(), "", false, getName() );
	}
	
	public void destroy() throws Exception {
		System.err.println( "destroy queue " + getName() );
		
		getServer().destroyQueue( getName() );
	}

}
