package org.torquebox.ruby.enterprise.tasks.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.ruby.enterprise.tasks.TaskMetaData;

public class TasksDeployer extends AbstractDeployer {
	
	public TasksDeployer() {
		setAllInputs( true );
		setStage( DeploymentStages.REAL );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		for ( TaskMetaData each : unit.getAllMetaData( TaskMetaData.class ) ) {
			deploy( unit, each );
		}
	}

	protected void deploy(DeploymentUnit unit, TaskMetaData each) {
		// TODO Auto-generated method stub
	}

}
