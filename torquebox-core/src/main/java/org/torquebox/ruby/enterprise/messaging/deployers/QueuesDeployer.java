package org.torquebox.ruby.enterprise.messaging.deployers;

import org.torquebox.ruby.enterprise.messaging.Queue;
import org.torquebox.ruby.enterprise.messaging.QueueMetaData;
import org.torquebox.ruby.enterprise.messaging.QueuesMetaData;

public class QueuesDeployer extends AbstractDestinationDeployer<QueueMetaData, QueuesMetaData> {

	public QueuesDeployer() {
		super(QueuesMetaData.class);
		setDestinationClass( Queue.class );
	}
	
}
