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

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.ManagedQueue;
import org.torquebox.messaging.metadata.QueueMetaData;

/**
 * <pre>
 * Stage: REAL
 *    In: QueueMetaData
 *   Out: ManagedQueue
 * </pre>
 * 
 */
public class ManagedQueueDeployer extends AbstractDeployer {

    public ManagedQueueDeployer() {
        setAllInputs( true );
        addOutput( BeanMetaData.class );
        setStage( DeploymentStages.REAL );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        for (QueueMetaData each : allMetaData) {
            deploy( unit, each );
        }
    }

    protected void deploy(DeploymentUnit unit, QueueMetaData metaData) {
        String beanName = AttachmentUtils.beanName( unit, ManagedQueue.class, metaData.getName() );

        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName, ManagedQueue.class.getName() );
        builder.addPropertyMetaData( "name", metaData.getName() );
        builder.addPropertyMetaData( "durable", metaData.isDurable() );

        ValueMetaData hornetServerInjection = builder.createInject( "JMSServerManager" );
        builder.addPropertyMetaData( "server", hornetServerInjection );

        AttachmentUtils.attach( unit, builder.getBeanMetaData() );
    }

}
