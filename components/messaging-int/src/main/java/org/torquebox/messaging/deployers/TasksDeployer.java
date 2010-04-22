package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.util.StringUtils;
import org.torquebox.messaging.metadata.DuplicateQueueException;
import org.torquebox.messaging.metadata.MessageDrivenConsumerConfig;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.QueuesMetaData;
import org.torquebox.messaging.metadata.TaskMetaData;

public class TasksDeployer extends AbstractDeployer {

	public TasksDeployer() {
		setStage( DeploymentStages.REAL );
		setAllInputs( true );
		addInput( TaskMetaData.class );
		addOutput( MessageDrivenConsumerConfig.class );
		addOutput( QueuesMetaData.class );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends TaskMetaData> allTasks = unit.getAllMetaData( TaskMetaData.class );
		
		for ( TaskMetaData each : allTasks ) {
			deploy( unit, each );
		}
		
	}

	protected void deploy(DeploymentUnit unit, TaskMetaData task) throws DeploymentException {
		QueuesMetaData queues = unit.getAttachment( QueuesMetaData.class );
		
		if ( queues == null ) {
			queues = new QueuesMetaData();
			unit.addAttachment( QueuesMetaData.class, queues );
		}
		
		String baseQueueName = task.getRubyClassName();
		
		if ( baseQueueName.endsWith( "Task" ) ) {
			baseQueueName = baseQueueName.substring( 0, baseQueueName.length() - 4 );
		}
		
		baseQueueName = StringUtils.underscore(baseQueueName);
		
		QueueMetaData queue = new QueueMetaData();
		queue.setName( "/queues/torquebox/tasks/" + baseQueueName );
		
		try {
			queues.addQueue( queue );
		} catch (DuplicateQueueException e) {
			throw new DeploymentException( e );
		}
		
		MessageDrivenConsumerConfig consumer = new MessageDrivenConsumerConfig();
		consumer.setDestinationName( queue.getName() );
		consumer.setRubyClassName( task.getRubyClassName() );
		unit.addAttachment( MessageDrivenConsumerConfig.class.getName() + "$" + queue.getName() + "$" + task.getRubyClassName(), consumer, MessageDrivenConsumerConfig.class );
	}

}
