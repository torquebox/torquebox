package org.torquebox.ruby.enterprise.messaging.deployers;

import org.torquebox.ruby.enterprise.messaging.Topic;
import org.torquebox.ruby.enterprise.messaging.TopicMetaData;
import org.torquebox.ruby.enterprise.messaging.TopicsMetaData;

public class TopicsDeployer extends AbstractDestinationDeployer<TopicMetaData, TopicsMetaData> {

	public TopicsDeployer() {
		super(TopicsMetaData.class);
		setDestinationClass( Topic.class );
	}

}
