/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.common.util.StringUtils;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.TaskMetaData;

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

        setInput( RubyApplicationMetaData.class );
        addInput( MessageProcessorMetaData.class );
        addInput( TaskMetaData.class );

        addOutput( MessageProcessorMetaData.class );
        addOutput( QueueMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends TaskMetaData> allTasks = unit.getAllMetaData( TaskMetaData.class );

        for (TaskMetaData each : allTasks) {
            deploy( unit, each );
        }

    }

    protected void deploy(DeploymentUnit unit, TaskMetaData task) throws DeploymentException {
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        String baseQueueName = task.getRubyClassName();
        if (baseQueueName.endsWith( "Task" )) {
            baseQueueName = baseQueueName.substring( 0, baseQueueName.length() - 4 );
        }
        baseQueueName = StringUtils.underscore( baseQueueName );

        QueueMetaData queue = new QueueMetaData();
        queue.setName( "/queues/torquebox/" + appMetaData.getApplicationName() + "/tasks/" + baseQueueName );
        AttachmentUtils.multipleAttach( unit, queue, queue.getName() );

        MessageProcessorMetaData processorMetaData = getMessageProcessorMetaData( unit, task.getRubyClassName() );
        processorMetaData.setDestinationName( queue.getName() );
        processorMetaData.setRubyClassName( task.getRubyClassName(), task.getLocation() );
        AttachmentUtils.multipleAttach( unit, processorMetaData, processorMetaData.getName() );
    }

    protected MessageProcessorMetaData getMessageProcessorMetaData(DeploymentUnit unit, String handlerName) {
        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData( MessageProcessorMetaData.class );

        for (MessageProcessorMetaData each : allMetaData) {
            if ("tasks".equals( each.getDestinationName() ) && 
                handlerName.equals( each.getRubyClassName() )) {
                return each;
            }
        }

        return new MessageProcessorMetaData();
    }
}
