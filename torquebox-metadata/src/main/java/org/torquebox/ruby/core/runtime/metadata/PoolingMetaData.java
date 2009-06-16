package org.torquebox.ruby.core.runtime.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PoolingMetaData {
	
	private Map<String,PoolMetaData> pools = new HashMap<String,PoolMetaData>();
	
	public PoolingMetaData() {
		
	}
	
	public void addPool(PoolMetaData pool) {
		this.pools.put( pool.getName(), pool );
	}
	
	public Collection<PoolMetaData> getPools() {
		return this.pools.values();
	}
	
	public PoolMetaData getPool(String name) {
		return this.pools.get( name );
	}

}
