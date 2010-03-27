package org.torquebox.messaging.core;


public class Queue extends Destination {
	
	public Queue() {
		
	}
	
	public void start() throws Exception {
		log.info( "start queue " + getName() );
		
		getServer().createQueue(getName(), getName(), null, true);
	}
	
	public void destroy() throws Exception {
		log.info( "destroy queue " + getName() );
		
		getServer().destroyQueue( getName() );
	}

}
