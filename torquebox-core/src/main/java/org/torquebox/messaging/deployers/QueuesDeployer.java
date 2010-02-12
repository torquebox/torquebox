package org.torquebox.messaging.deployers;

import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.QueuesMetaData;
import org.torquebox.ruby.enterprise.messaging.Queue;

public class QueuesDeployer extends AbstractDestinationDeployer<QueueMetaData, QueuesMetaData> {

	public QueuesDeployer() {
		super(QueuesMetaData.class);
		setDestinationClass( Queue.class );
	}
	
}
