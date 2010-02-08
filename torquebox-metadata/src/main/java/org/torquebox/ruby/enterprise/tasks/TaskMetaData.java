package org.torquebox.ruby.enterprise.tasks;

import org.torquebox.ruby.enterprise.messaging.DestinationMetaData;

public class TaskMetaData {
	
	private String className;
	private DestinationMetaData destination;
	private String classLocation;

	public TaskMetaData() {
		
	}
	
	public void setClassLocation(String classLocation)  {
		this.classLocation = classLocation;
	}
	
	public String getClassLocation() {
		return this.classLocation;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public void setDestination(DestinationMetaData destination) {
		this.destination = destination;
	}
	
	public DestinationMetaData getDestination() {
		return this.destination;
	}

}
