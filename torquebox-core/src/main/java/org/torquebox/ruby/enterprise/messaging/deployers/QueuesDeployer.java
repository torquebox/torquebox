package org.torquebox.ruby.enterprise.messaging.deployers;

import org.jboss.jms.server.destination.QueueService;
import org.torquebox.ruby.enterprise.messaging.QueueMetaData;
import org.torquebox.ruby.enterprise.messaging.QueuesMetaData;

public class QueuesDeployer extends AbstractDestinationDeployer<QueueMetaData, QueuesMetaData> {

	public QueuesDeployer() {
		super(QueuesMetaData.class);
		setService( "Queue" );
		setCode( QueueService.class.getName() );
	}
	
	public String getObjectName(String queueName) {
		return "jboss.messaging.destination:service=Queue,name=" + queueName;
	}

}
