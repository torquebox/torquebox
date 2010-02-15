package org.torquebox.messaging;

public class Topic extends Destination {
	
	public Topic() {
		
	}
	
	public void start() throws Exception {
		log.info( "start topic " + getName() );
		
		getServer().createTopic(getName(), "topics/" + getName());
	}
	
	public void stop() throws Exception {
		log.info( "stop topic " + getName() );
		
		getServer().destroyTopic( getName() );
	}

}
