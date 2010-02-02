package org.torquebox.ruby.enterprise.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DestinationsMetaData<T extends DestinationMetaData> {
	
	private Map<String,T> destinations = new HashMap<String,T>();
	
	public DestinationsMetaData() {
		
	}
	
	public void addDestination(T destination) throws DuplicateQueueException {
		if ( destinations.containsKey( destination.getName() ) ) {
			throw new DuplicateQueueException( destination.getName() );
		}
		this.destinations.put( destination.getName(), destination );
	}
	
	public Collection<T> getDestinations() {
		return this.destinations.values();
	}
	
	public T getDestination(String name) {
		return this.destinations.get( name );
	}

}
