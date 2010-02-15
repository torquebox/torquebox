package org.torquebox.ruby.enterprise.queues.metadata;

import java.util.ArrayList;
import java.util.List;

public class RubyTaskQueuesMetaData {
	
	private List<RubyTaskQueueMetaData> queues = new ArrayList<RubyTaskQueueMetaData>();
	
	public RubyTaskQueuesMetaData() {
	}
	
	public void addQueue(RubyTaskQueueMetaData queue) {
		this.queues.add( queue );
	}
	
	public List<RubyTaskQueueMetaData> getQueues() {
		return this.queues;
	}
	
	public RubyTaskQueueMetaData getQueueByClassName(String queueClassName) {
		for ( RubyTaskQueueMetaData queue : this.queues ) {
			if ( queue.getQueueClassName().equals( queueClassName ) ) { 
				return queue;
			}
		}
		
		return null;
	}
	
	public int size() {
		return this.queues.size();
	}
	
	public boolean empty() {
		return size() == 0;
	}

}
