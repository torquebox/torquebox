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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.common.util.StringUtils;
import org.torquebox.interp.deployers.AbstractRubyScanningDeployer;
import org.torquebox.messaging.metadata.TaskMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: suffix and path from jboss-beans.xml
 *   Out: TaskMetaData
 * </pre>
 * 
 */
public class TasksScanningDeployer extends AbstractRubyScanningDeployer {

    public TasksScanningDeployer() {

    }

    @Override
    protected void deploy(VFSDeploymentUnit unit, VirtualFile file, String relativePath) throws DeploymentException {
        log.info( "deploying " + relativePath );

        TaskMetaData taskMetaData = new TaskMetaData();

        String simpleLocation = getPath() + relativePath.substring( 0, relativePath.length() - 3 );

        taskMetaData.setLocation( simpleLocation );
        taskMetaData.setRubyClassName( StringUtils.pathToClassName( relativePath, ".rb" ) );

        unit.addAttachment( TaskMetaData.class.getName() + "$" + simpleLocation, taskMetaData, TaskMetaData.class );
    }

}
