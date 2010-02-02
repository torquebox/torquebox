package org.torquebox.ruby.enterprise.messaging;

public class DestinationMetaData {

	String name;
	
	public DestinationMetaData() {
		
	}
	
	public DestinationMetaData(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
