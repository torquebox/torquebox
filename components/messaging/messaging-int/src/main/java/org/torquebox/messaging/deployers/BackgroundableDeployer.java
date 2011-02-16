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
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;

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

        setInput( RubyApplicationMetaData.class );
        addInput( MessageProcessorMetaData.class );

        addOutput( MessageProcessorMetaData.class );
        addOutput( QueueMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.class );     
        MessageProcessorMetaData processorMetaData = getMessageProcessorMetaData( unit );
        String appName = appMetaData.getApplicationName();

        if (processorMetaData.getConcurrency() > 0) {
            log.info( "Setting up Backgroundable queue and message processor for " + 
                      appName + " with a concurrency of " + 
                      processorMetaData.getConcurrency().toString() );
            QueueMetaData queue = new QueueMetaData();
            String queueName = "/queues/torquebox/" + appName + "/backgroundable";
            queue.setName( queueName );
            AttachmentUtils.multipleAttach( unit, queue, queue.getName() );


            processorMetaData.setDestinationName( queue.getName() );
            processorMetaData.setRubyClassName( "TorqueBox::Messaging::BackgroundableProcessor" );
            processorMetaData.setRubyRequirePath( "torquebox/messaging/backgroundable_processor" );
            AttachmentUtils.multipleAttach( unit, processorMetaData, processorMetaData.getName() );
        } else {
            log.warn( "Backgroundable concurrency is 0, disabling queue and message processor for " + appName );
            unit.removeAttachment( MessageProcessorMetaData.class.getName() + "$" + processorMetaData.getName(), 
                                   MessageProcessorMetaData.class );
        }
    }

    protected MessageProcessorMetaData getMessageProcessorMetaData(DeploymentUnit unit) {
        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData( MessageProcessorMetaData.class );

        for (MessageProcessorMetaData each : allMetaData) {
            if ("tasks".equals( each.getDestinationName() ) && 
                "Backgroundable".equals( each.getRubyClassName() )) {
                return each;
            }
        }
        return new MessageProcessorMetaData();
    }
}
