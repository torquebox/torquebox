package org.torquebox.messaging.metadata;

import java.util.Collection;

public class QueuesMetaData extends DestinationsMetaData<QueueMetaData>{
	
	public QueuesMetaData() {
		
	}
	
	public Collection<QueueMetaData> getQueues() {
		return getDestinations();
	}
	
	public QueueMetaData getQueue(String name) {
		return getDestination(name);
	}
	
	public void addQueue(QueueMetaData queue) throws DuplicateQueueException {
		addDestination( queue );
	}

}
