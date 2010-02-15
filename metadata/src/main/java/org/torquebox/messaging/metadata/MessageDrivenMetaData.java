package org.torquebox.messaging.metadata;

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
