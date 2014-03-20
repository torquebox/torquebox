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

package org.torquebox.messaging.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.messaging.tasks.TaskMetaData;

/**
 * <pre>
 * Stage: PRE_PARSE
 *    In: RubyApplicationMetaData
 *   Out: TaskMetaData
 * </pre>
 * 
 * Injects a TaskMetaData for the Backgroundable queue for
 * TasksYamlParsingDeployer to pick up and complete. If TYPD doesn't find any
 * backgroundable options, the TMD will still be available for TasksDeployer to
 * handle.
 */
public class BackgroundablePresetsProcessor implements DeploymentUnitProcessor {

    public BackgroundablePresetsProcessor() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (DeploymentUtils.isUnitRootless( unit )) {
            return;
        }
        if (!unit.hasAttachment( RubyAppMetaData.ATTACHMENT_KEY )) {
            return;
        }
        TaskMetaData task = new TaskMetaData();

        task.setRubyClassName( "TorqueBox::Messaging::BackgroundableProcessor" );
        task.setLocation( "torquebox/messaging/backgroundable_processor" );
        task.setQueueSuffix( "torquebox_backgroundable" );
        task.setSimpleName( "Backgroundable" );
        task.setXAEnabled( false );

        unit.addToAttachmentList( TaskMetaData.ATTACHMENTS_KEY, task );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
}
