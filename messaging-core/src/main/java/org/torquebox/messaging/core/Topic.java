package org.torquebox.messaging.core;

public class Topic extends Destination {
	
	public Topic() {
		
	}
	
	public void start() throws Exception {
		log.info( "start topic " + getName() );
		
		getServer().createTopic(getName(), getName());
	}
	
	public void destroy() throws Exception {
		log.info( "destroy topic " + getName() );
		
		getServer().destroyTopic( getName() );
	}

}
