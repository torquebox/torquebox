package org.torquebox.messaging;

public class Topic extends Destination {
	
	public Topic() {
		
	}
	
	public void start() throws Exception {
		log.info( "start topic " + getName() );
		
		getServer().createTopic(getName(), getName());
	}
	
	public void stop() throws Exception {
		log.info( "stop topic " + getName() );
		
		getServer().destroyTopic( getName() );
	}

}
