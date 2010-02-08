package org.torquebox.ruby.enterprise.tasks.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.ruby.enterprise.messaging.QueueMetaData;
import org.torquebox.ruby.enterprise.tasks.TaskMetaData;

public class DefaultTaskQueuesDeployer extends AbstractDeployer {
	
	public DefaultTaskQueuesDeployer() {
		setStage( DeploymentStages.DESCRIBE );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		for ( TaskMetaData each : unit.getAllMetaData( TaskMetaData.class ) ) {
			deploy( unit, each );
		}
	}

	protected void deploy(DeploymentUnit unit, TaskMetaData task) {
		if ( task.getDestination() != null ) {
			return;
		}
		
		QueueMetaData defaultQueue = new QueueMetaData();
		task.setDestination( defaultQueue );
	}

}
