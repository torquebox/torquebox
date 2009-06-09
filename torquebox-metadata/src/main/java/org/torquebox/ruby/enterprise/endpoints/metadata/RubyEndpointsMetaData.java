package org.torquebox.ruby.enterprise.endpoints.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RubyEndpointsMetaData {
	
	private Map<String, RubyEndpointMetaData> endpoints = new HashMap<String,RubyEndpointMetaData>();

	public RubyEndpointsMetaData() {
	}
	
	public void addEndpoint(RubyEndpointMetaData endpoint) {
		this.endpoints.put( endpoint.getName(), endpoint );
	}
	
	public RubyEndpointMetaData getEndpointByName(String name) {
		return this.endpoints.get( name );
	}
	
	public Collection<RubyEndpointMetaData> getEndpoints() {
		return this.endpoints.values();
	}
	

}
