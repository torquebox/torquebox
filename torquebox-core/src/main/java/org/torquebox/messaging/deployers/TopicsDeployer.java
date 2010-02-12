package org.torquebox.messaging.deployers;

import org.torquebox.messaging.metadata.TopicMetaData;
import org.torquebox.messaging.metadata.TopicsMetaData;
import org.torquebox.ruby.enterprise.messaging.Topic;

public class TopicsDeployer extends AbstractDestinationDeployer<TopicMetaData, TopicsMetaData> {

	public TopicsDeployer() {
		super(TopicsMetaData.class);
		setDestinationClass( Topic.class );
	}

}
