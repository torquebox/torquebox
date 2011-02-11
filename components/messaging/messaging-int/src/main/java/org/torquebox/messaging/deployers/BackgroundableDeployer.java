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
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.rack.metadata.RackApplicationMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: QueueMetaData, MessageProcessorMetaData
 * </pre>
 * 
 * Any object can have tasks!
 */
public class BackgroundableDeployer extends AbstractDeployer {

    public BackgroundableDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        setInput( RackApplicationMetaData.class );
        addOutput( MessageProcessorMetaData.class );
        addOutput( QueueMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );

        QueueMetaData queue = new QueueMetaData();
        String queueName = "/queues/torquebox/" + rackMetaData.getRackApplicationName() + "/backgroundable";
        queue.setName( queueName );
        AttachmentUtils.multipleAttach( unit, queue, queue.getName() );

        // TODO: allow for configurable concurrency
        MessageProcessorMetaData processorMetaData = new MessageProcessorMetaData();
        processorMetaData.setDestinationName( queue.getName() );
        processorMetaData.setRubyClassName( "TorqueBox::Messaging::BackgroundableProcessor" );
        processorMetaData.setRubyRequirePath( "torquebox/messaging/backgroundable_processor" );
        AttachmentUtils.multipleAttach( unit, processorMetaData, processorMetaData.getName() );
    }

}
