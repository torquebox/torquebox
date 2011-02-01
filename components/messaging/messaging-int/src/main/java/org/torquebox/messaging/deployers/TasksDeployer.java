package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.util.StringUtils;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.TaskMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: TaskMetaData
 *   Out: QueueMetaData, MessageProcessorMetaData
 * </pre>
 *
 * Tasks are really sugar-frosted queues
 */
public class TasksDeployer extends AbstractDeployer {

    public TasksDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        setAllInputs( true );
        addInput( TaskMetaData.class );
        addOutput( MessageProcessorMetaData.class );
        addOutput( QueueMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends TaskMetaData> allTasks = unit.getAllMetaData( TaskMetaData.class );
                
        for ( TaskMetaData each : allTasks ) {
            deploy( unit, each );
        }
                
    }

    protected void deploy(DeploymentUnit unit, TaskMetaData task) throws DeploymentException {
        RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.class);
        String baseQueueName = task.getRubyClassName();
        if ( baseQueueName.endsWith( "Task" ) ) {
            baseQueueName = baseQueueName.substring( 0, baseQueueName.length() - 4 );
        }
        baseQueueName = StringUtils.underscore(baseQueueName);

        QueueMetaData queue = new QueueMetaData();
        queue.setName( "/queues/torquebox/" + rackMetaData.getRackApplicationName() + "/tasks/" + baseQueueName );
        AttachmentUtils.multipleAttach(unit, queue, queue.getName() );
                
        MessageProcessorMetaData processorMetaData = new MessageProcessorMetaData();
        processorMetaData.setDestinationName( queue.getName() );
        processorMetaData.setRubyClassName( task.getRubyClassName(), task.getLocation() );
        AttachmentUtils.multipleAttach(unit, processorMetaData, processorMetaData.getName() );
    }

}
