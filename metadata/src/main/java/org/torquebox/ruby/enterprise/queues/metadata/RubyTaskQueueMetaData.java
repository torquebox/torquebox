package org.torquebox.ruby.enterprise.queues.metadata;

public class RubyTaskQueueMetaData {
	
	private String queueClassName;
	
	private boolean enabled;

	private String queueClassLocation;

	public RubyTaskQueueMetaData() {
		this.enabled=true;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setQueueClassName(String queueClassName) {
		this.queueClassName = queueClassName;
	}
	
	public String getQueueClassName() {
		return this.queueClassName;
	}
	
	public void setQueueClassLocation(String queueClassLocation) {
		this.queueClassLocation = queueClassLocation;
	}
	
	public String getQueueClassLocation() {
		return this.queueClassLocation;
	}

	
	public String toString() {
		return "[RubyTaskQueue: queueClassName=" + queueClassName + "; enabled=" + enabled + "]";
	}

}
