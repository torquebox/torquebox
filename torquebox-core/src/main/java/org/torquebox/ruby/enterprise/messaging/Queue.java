package org.torquebox.ruby.enterprise.messaging;

import org.hornetq.api.core.management.HornetQServerControl;
import org.jboss.logging.Logger;

public class Queue {
	
	private static final Logger log = Logger.getLogger( Queue.class );
	
	private HornetQServerControl server;
	private String name;

	public Queue() {
		
	}
	
	public void setServer(HornetQServerControl server) {
		this.server = server;
	}
	
	public HornetQServerControl getServer() {
		return this.server;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void start() throws Exception {
		log.info( "start queue " + this.name );
		
		getServer().createQueue( this.name, "queues/" + this.name );
	}
	
	public void stop() throws Exception {
		log.info( "stop queue " + this.name );
		
		getServer().destroyQueue( this.name );
	}

}
