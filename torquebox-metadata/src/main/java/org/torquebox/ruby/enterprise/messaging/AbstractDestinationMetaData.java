package org.torquebox.ruby.enterprise.messaging;

public abstract class AbstractDestinationMetaData {
	
	private String name;
	
	public AbstractDestinationMetaData() {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

}
