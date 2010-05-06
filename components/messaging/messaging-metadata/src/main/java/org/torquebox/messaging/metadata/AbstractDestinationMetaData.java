package org.torquebox.messaging.metadata;

public class AbstractDestinationMetaData {

	String name;
	
	public AbstractDestinationMetaData() {
		
	}
	
	public AbstractDestinationMetaData(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
