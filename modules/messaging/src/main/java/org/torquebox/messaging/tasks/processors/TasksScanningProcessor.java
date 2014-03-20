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

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.vfs.VirtualFile;
import org.projectodd.polyglot.core.processors.AbstractScanningProcessor;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.util.StringUtils;
import org.torquebox.messaging.tasks.TaskMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: suffix and path from jboss-beans.xml
 *   Out: TaskMetaData
 * </pre>
 * 
 */
public class TasksScanningProcessor extends AbstractScanningProcessor {

    public TasksScanningProcessor() {
        addPath( "app/tasks/" );
        addPath( "tasks/" );
        setSuffixFilter( "_task.rb" );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @Override
    protected void deploy(DeploymentUnit unit, VirtualFile file, String parentPath, String relativePath) throws DeploymentUnitProcessingException {
        String rubyClassName = StringUtils.pathToClassName( relativePath, ".rb" );
        TaskMetaData taskMetaData = existingTaskMetaData( unit, rubyClassName );

        if (taskMetaData == null) {
            taskMetaData = new TaskMetaData();
            unit.addToAttachmentList( TaskMetaData.ATTACHMENTS_KEY, taskMetaData );
        }
        
        String simpleLocation = parentPath + relativePath.substring( 0, relativePath.length() - 3 );

        taskMetaData.setLocation( simpleLocation );
        taskMetaData.setRubyClassName( rubyClassName );
    }
    
    protected TaskMetaData existingTaskMetaData(DeploymentUnit unit, String rubyClassName) {
        AttachmentList<TaskMetaData> allMetaData = unit.getAttachment( TaskMetaData.ATTACHMENTS_KEY );
        if (allMetaData != null) {
            for (TaskMetaData each : allMetaData) {
                if (rubyClassName.equals( each.getRubyClassName() )) {
                    return each;
                }
            }
        }
        
        return null;
    }

}
