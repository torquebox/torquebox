package org.torquebox.messaging.metadata;

import java.util.Collection;

public class TopicsMetaData extends DestinationsMetaData<TopicMetaData>{
	
	public TopicsMetaData() {
		
	}
	
	public Collection<TopicMetaData> getTopics() {
		return getDestinations();
	}
	
	public TopicMetaData getTopic(String name) {
		return getDestination(name);
	}
	
	public void addTopic(TopicMetaData topic) throws DuplicateQueueException {
		addDestination( topic );
	}

}
