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
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.TaskMetaData;

/**
 * <pre>
 * Stage: PRE_PARSE
 *    In: RubyApplicationMetaData
 *   Out: TaskMetaData
 * </pre>
 * 
 * Injects a TaskMetaData for the Backgroundable queue for TasksYamlParsingDeployer to 
 * pick up and complete. If TYPD doesn't find any backgroundable options, the
 * TMD will still be available for TasksDeployer to handle.
 */
public class BackgroundableDeployer extends AbstractDeployer {

    public BackgroundableDeployer() {
        setStage( DeploymentStages.PRE_PARSE );
        setInput( RubyApplicationMetaData.class );
        setOutput( TaskMetaData.class );
    }

     @Override
     public void deploy(DeploymentUnit unit) throws DeploymentException {
         TaskMetaData task = new TaskMetaData();
         
         task.setRubyClassName( "TorqueBox::Messaging::BackgroundableProcessor" );
         task.setLocation( "torquebox/messaging/backgroundable_processor" );
         task.setQueueSuffix( "/backgroundable" );
         task.setSimpleName( "Backgroundable" );

         AttachmentUtils.multipleAttach( unit, task, task.getName() );
     }
}
