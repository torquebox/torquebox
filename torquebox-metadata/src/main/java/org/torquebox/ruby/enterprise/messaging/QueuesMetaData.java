package org.torquebox.ruby.enterprise.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QueuesMetaData {
	
	private Map<String,QueueMetaData> queues = new HashMap<String,QueueMetaData>();
	
	public QueuesMetaData() {
		
	}
	
	public void addQueue(QueueMetaData queue) throws DuplicateQueueException {
		if ( queues.containsKey( queue.getName() ) ) {
			throw new DuplicateQueueException( queue.getName() );
		}
		this.queues.put( queue.getName(), queue );
	}
	
	public Collection<QueueMetaData> getQueues() {
		return this.queues.values();
	}
	
	public QueueMetaData getQueue(String name) {
		return this.queues.get( name );
	}

}
