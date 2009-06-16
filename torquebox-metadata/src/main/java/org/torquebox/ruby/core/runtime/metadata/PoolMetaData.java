package org.torquebox.ruby.core.runtime.metadata;

public class PoolMetaData {
	
	private String name;
	
	private int minimumSize;
	private int maximumSize;

	public PoolMetaData() {
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setMinimumSize(int minimumSize) {
		this.minimumSize = minimumSize;
	}
	
	public int getMinimumSize() {
		return this.minimumSize;
	}
	
	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
	}
	
	public int getMaximumSize() {
		return this.maximumSize;
	}
	
	public String toString() {
		return "[PoolMetaData: name=" + this.name + "; min=" + this.minimumSize + "; max=" + this.maximumSize + "]";
	}

	public void setShared() {
		this.minimumSize = -1;
		this.maximumSize = -1;
	}
	
	public boolean isShared() {
		return ( this.minimumSize < 0 && this.maximumSize < 0 );
	}
	
}
