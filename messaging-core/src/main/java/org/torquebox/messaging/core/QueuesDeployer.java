package org.torquebox.messaging.core;

import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.QueuesMetaData;

public class QueuesDeployer extends AbstractDestinationDeployer<QueueMetaData, QueuesMetaData> {

	public QueuesDeployer() {
		super(QueuesMetaData.class);
		setDestinationClass( Queue.class );
	}
	
}
