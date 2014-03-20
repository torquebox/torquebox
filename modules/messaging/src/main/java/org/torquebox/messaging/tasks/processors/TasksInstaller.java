/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.messaging.tasks.processors;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.projectodd.polyglot.messaging.destinations.QueueMetaData;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.messaging.tasks.TaskMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: TaskMetaData
 *   Out: QueueMetaData, MessageProcessorMetaData
 * </pre>
 *
 * Tasks are really sugar-frosted queues
 */
public class TasksInstaller implements DeploymentUnitProcessor {

    public TasksInstaller() {
    }

    @Override
    
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        RubyAppMetaData appMetaData = unit.getAttachment( RubyAppMetaData.ATTACHMENT_KEY );
        
        if ( appMetaData == null ) {
            return;
        }
        
        List<TaskMetaData> allTasks = unit.getAttachmentList( TaskMetaData.ATTACHMENTS_KEY);

        for (TaskMetaData each : allTasks) {
            deploy( phaseContext, unit, appMetaData, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, DeploymentUnit unit, RubyAppMetaData appMetaData, TaskMetaData task) throws DeploymentUnitProcessingException {
        String queueName = "/queues/torquebox/" + appMetaData.getApplicationName() + "/tasks/" + task.getQueueSuffix();
                
        if (task.getConcurrency() > 0) {
            QueueMetaData queue = new QueueMetaData();
            queue.setName( queueName );
            queue.setDurable( task.isDurable() );
            unit.addToAttachmentList( QueueMetaData.ATTACHMENTS_KEY, queue );

            MessageProcessorMetaData processorMetaData = new MessageProcessorMetaData();
            processorMetaData.setDestinationName( queueName );
            processorMetaData.setRubyClassName( task.getRubyClassName(), task.getLocation() );
            processorMetaData.setConcurrency( task.getConcurrency() );
            processorMetaData.setMessageSelector( "JMSCorrelationID IS NULL" );
            processorMetaData.setXAEnabled( task.isXAEnabled() );
            unit.addToAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY, processorMetaData );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.messaging.tasks"  );

}
