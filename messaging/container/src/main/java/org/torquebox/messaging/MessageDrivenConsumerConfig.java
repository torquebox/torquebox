package org.torquebox.messaging;

public class MessageDrivenConsumerConfig {
	
	private String rubyClassName;
	private String destinationName;

	public MessageDrivenConsumerConfig() {
	}
	
	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}
	
	public String getRubyClassName() {
		return this.rubyClassName;
	}
	
	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}
	
	public String getDestinationName() {
		return this.destinationName;
	}

}
