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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;

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
public class BackgroundablePresetsDeployer implements DeploymentUnitProcessor {

    public BackgroundablePresetsDeployer() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        if (!unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY )) {
            return;
        }
        TaskMetaData task = new TaskMetaData();

        task.setRubyClassName( "TorqueBox::Messaging::BackgroundableProcessor" );
        task.setLocation( "torquebox/messaging/backgroundable_processor" );
        task.setQueueSuffix( "-torquebox-backgroundable" );
        task.setSimpleName( "Backgroundable" );

        unit.addToAttachmentList( TaskMetaData.ATTACHMENTS_KEY, task );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }
}
