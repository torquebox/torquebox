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

package org.torquebox.messaging.destinations.processors;

import java.util.Map;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.projectodd.polyglot.messaging.destinations.QueueMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: queues.yml
 *   Out: QueueMetaData
 * </pre>
 * <p/>
 * Creates QueueMetaData instances from queues.yml
 */
public class QueuesYamlParsingProcessor extends AbstractDestinationYamlParsingProcessor {

    public QueuesYamlParsingProcessor() {
        super();
        setSectionName("queues");
    }

    @SuppressWarnings("unchecked")
    public void parse(DeploymentUnit unit, Object dataObject) throws DeploymentUnitProcessingException {

        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) dataObject;

        for (String queueName : data.keySet()) {
            QueueMetaData queueMetaData = new QueueMetaData(queueName);
            Map<String, Object> queueOptions = data.get(queueName);

            // Default value for durability
            queueMetaData.setDurable(true);

            if (queueOptions != null) {
                if (queueOptions.containsKey("durable")) {
                    queueMetaData.setDurable((Boolean) queueOptions.get("durable"));
                }

                if (queueOptions.containsKey("exported")) {
                    queueMetaData.setExported((Boolean) queueOptions.get("exported"));
                }

                parseRemote(queueMetaData, queueOptions.get("remote"));
            }

            unit.addToAttachmentList(QueueMetaData.ATTACHMENTS_KEY, queueMetaData);
        }
    }
}
