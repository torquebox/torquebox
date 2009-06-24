package org.torquebox.ruby.core.runtime.metadata;

public class PoolMetaData {
	
	public enum PoolType {
		NON_SHARED,
		SHARED,
		GLOBAL,
	}
	
	private String name;
	
	private PoolType poolType;
	private int minimumSize;
	private int maximumSize;
	
	public PoolMetaData() {
		this.poolType = PoolType.NON_SHARED;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setMinimumSize(int minimumSize) {
		this.minimumSize = minimumSize;
		this.poolType = PoolType.NON_SHARED;
	}
	
	public int getMinimumSize() {
		return this.minimumSize;
	}
	
	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
		this.poolType = PoolType.NON_SHARED;
	}
	
	public int getMaximumSize() {
		return this.maximumSize;
	}
	
	public String toString() {
		return "[PoolMetaData: name=" + this.name + "; min=" + this.minimumSize + "; max=" + this.maximumSize + "]";
	}

	public void setShared() {
		this.poolType = PoolType.SHARED;
		this.minimumSize = -1;
		this.maximumSize = -1;
	}
	
	public boolean isShared() {
		return ( this.poolType == PoolType.SHARED );
	}

	public void setGlobal() {
		this.poolType = PoolType.GLOBAL;
		this.minimumSize = -1;
		this.maximumSize = -1;
	}
	
	public boolean isGlobal() {
		return ( this.poolType == PoolType.GLOBAL );
	}
	
}
