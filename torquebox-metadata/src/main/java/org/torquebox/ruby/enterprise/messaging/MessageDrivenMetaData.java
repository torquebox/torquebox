package org.torquebox.ruby.enterprise.messaging;

public class MessageDrivenMetaData {

	private String destinationName;

	public MessageDrivenMetaData() {
		
	}
	
	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}
	
	public String getDestinationName() {
		return this.destinationName;
	}
}
