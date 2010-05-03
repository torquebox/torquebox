package org.torquebox.messaging.metadata;

public class MessageProcessorMetaData {
	
	private String rubyClassName;
	private String rubyRequirePath;
	private String destinationName;

	public MessageProcessorMetaData() {
	}
	
	public String getName() {
		return ( this.destinationName + "." + this.rubyClassName );
	}
	
	public void setRubyClassName(String rubyClassName, String rubyRequirePath) {
		this.rubyClassName  = rubyClassName;
		this.rubyRequirePath = rubyRequirePath;
	}
	
	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}
	
	public String getRubyClassName() {
		return this.rubyClassName;
	}
	
	public void setRubyRequirePath(String rubyRequirePath) {
		this.rubyRequirePath = rubyRequirePath;
	}
	
	public String getRubyRequirePath() {
		return this.rubyRequirePath;
	}
	
	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}
	
	public String getDestinationName() {
		return this.destinationName;
	}
	

}
