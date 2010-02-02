package org.torquebox.ruby.enterprise.messaging.deployers;

import org.jboss.jms.server.destination.TopicService;
import org.torquebox.ruby.enterprise.messaging.TopicMetaData;
import org.torquebox.ruby.enterprise.messaging.TopicsMetaData;

public class TopicsDeployer extends AbstractDestinationDeployer<TopicMetaData, TopicsMetaData> {

	public TopicsDeployer() {
		super(TopicsMetaData.class);
		setService( "Topic" );
		setCode( TopicService.class.getName() );
	}

	public String getObjectName(String topicName) {
		return "jboss.messaging.destination:service=Topic,name=" + topicName;
	}

}
