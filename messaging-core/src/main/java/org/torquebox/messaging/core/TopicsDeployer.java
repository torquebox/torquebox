package org.torquebox.messaging.core;

import org.torquebox.messaging.metadata.TopicMetaData;
import org.torquebox.messaging.metadata.TopicsMetaData;

public class TopicsDeployer extends AbstractDestinationDeployer<TopicMetaData, TopicsMetaData> {

	public TopicsDeployer() {
		super(TopicsMetaData.class);
		setDestinationClass( Topic.class );
	}

}
