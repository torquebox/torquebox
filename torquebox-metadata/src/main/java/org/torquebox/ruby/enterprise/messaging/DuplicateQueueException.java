package org.torquebox.ruby.enterprise.messaging;

public class DuplicateQueueException extends DuplicateDestinationException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateQueueException(String name) {
		super( name );
	}
	
	public String toString() {
		return "Already a queue named '" + getName() + "'"; 
	}

}
