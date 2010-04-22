package org.torquebox.messaging.metadata;

public class DuplicateQueueException extends DuplicateDestinationException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateQueueException(String name) {
		super( name );
	}
	
	public String toString() {
		return "Duplicate queue name '" + getName() + "'"; 
	}

}
