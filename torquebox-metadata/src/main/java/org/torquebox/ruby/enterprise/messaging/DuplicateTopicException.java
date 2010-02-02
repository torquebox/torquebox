package org.torquebox.ruby.enterprise.messaging;

public class DuplicateTopicException extends DuplicateDestinationException {
	
	private static final long serialVersionUID = 1L;

	public DuplicateTopicException(String name) {
		super( name );
	}
	
	public String toString() {
		return "Duplicate topic name '" + getName() + "'"; 
	}

}
