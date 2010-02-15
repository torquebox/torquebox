package org.torquebox.messaging.metadata;

public class DuplicateDestinationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String name;

	public DuplicateDestinationException(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return "Duplicate destination name '" + getName() + "'";
	}
}
