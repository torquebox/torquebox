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

package org.torquebox.messaging;

import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentException;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: TaskMetaData
 *   Out: QueueMetaData, MessageProcessorMetaData
 * </pre>
 *
 * Tasks are really sugar-frosted queues
 */
public class TasksDeployer implements DeploymentUnitProcessor {

    public TasksDeployer() {
    }

    @Override
    
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        
        if ( appMetaData == null ) {
            return;
        }
        
        List<TaskMetaData> allTasks = unit.getAttachmentList( TaskMetaData.ATTACHMENTS_KEY);

        for (TaskMetaData each : allTasks) {
            deploy( phaseContext, unit, appMetaData, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, DeploymentUnit unit, RubyApplicationMetaData appMetaData, TaskMetaData task) throws DeploymentUnitProcessingException {
        String queueName = "queue/" + appMetaData.getApplicationName() + "-tasks" + task.getQueueSuffix();
        
        if (task.getConcurrency() > 0) {
            System.err.println( "task queue: " + queueName );
            QueueMetaData queue = new QueueMetaData();
            queue.setName( queueName );
            unit.addToAttachmentList( QueueMetaData.ATTACHMENT_KEY, queue );

            MessageProcessorMetaData processorMetaData = new MessageProcessorMetaData();
            processorMetaData.setDestinationName( queueName );
            processorMetaData.setRubyClassName( task.getRubyClassName(), task.getLocation() );
            processorMetaData.setConcurrency( task.getConcurrency() );
            unit.addToAttachmentList( MessageProcessorMetaData.ATTACHMENT_KEY, processorMetaData );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
