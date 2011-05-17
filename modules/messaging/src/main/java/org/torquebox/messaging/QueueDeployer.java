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

import org.hornetq.jms.server.JMSServerManager;
import org.jboss.as.messaging.jms.JMSServices;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * <pre>
 * Stage: REAL
 *    In: QueueMetaData
 *   Out: ManagedQueue
 * </pre>
 * 
 */
public class QueueDeployer implements DeploymentUnitProcessor {

    public QueueDeployer() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENT_KEY );

        for (QueueMetaData each : allMetaData) {
            deploy( phaseContext, each );
        }

    }

    protected void deploy(DeploymentPhaseContext phaseContext, QueueMetaData queue) {
        final JMSQueueService service = new JMSQueueService(queue.getName(), null, queue.isDurable(), new String[] { queue.getBindName() } );
        final ServiceName serviceName = phaseContext.getDeploymentUnit().getServiceName().append( JMSServices.JMS_QUEUE_BASE ).append( queue.getName() );
        System.err.println( "install queue: " + serviceName  + " bind to " + queue.getName() );
        phaseContext.getServiceTarget().addService(serviceName, service)
                .addDependency(JMSServices.JMS_MANAGER, JMSServerManager.class, service.getJmsServer())
                .setInitialMode(Mode.ACTIVE)
                .install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
