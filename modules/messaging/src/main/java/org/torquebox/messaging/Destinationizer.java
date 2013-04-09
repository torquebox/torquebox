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

package org.torquebox.messaging;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.projectodd.polyglot.core.AtRuntimeInstaller;
import org.projectodd.polyglot.core.HasStartStopLatches;
import org.projectodd.polyglot.messaging.destinations.DestinationUtils;
import org.projectodd.polyglot.messaging.destinations.Destroyable;
import org.projectodd.polyglot.messaging.destinations.DestroyableJMSQueueService;
import org.projectodd.polyglot.messaging.destinations.DestroyableJMSTopicService;
import org.projectodd.polyglot.messaging.destinations.QueueMetaData;
import org.projectodd.polyglot.messaging.destinations.TopicMetaData;
import org.projectodd.polyglot.messaging.destinations.processors.QueueInstaller;
import org.projectodd.polyglot.messaging.destinations.processors.TopicInstaller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to manage the destinations at runtime. It allows to create and remove destinations (queues and topics).
 *
 * @author Marek Goldmann
 */
public class Destinationizer extends AtRuntimeInstaller<Destinationizer> {

    public Destinationizer(DeploymentUnit unit, ServiceTarget globalServiceTarget) {
        super(unit, globalServiceTarget);
    }

    /**
     * With the service start the queues and topics listed in the metadata are deployed.
     *
     * @param context
     * @throws StartException
     * @see QueueMetaData
     * @see TopicMetaData
     */
    @Override
    public void start(StartContext context) throws StartException {
        super.start(context);

        List<QueueMetaData> queueMetaDatas = getUnit().getAttachmentList(QueueMetaData.ATTACHMENTS_KEY);

        if (queueMetaDatas != null && queueMetaDatas.size() > 0) {

            log.debugf("Installing %s queues listed in deployment descriptors...", queueMetaDatas.size());

            for (QueueMetaData metaData : queueMetaDatas) {
                if (!metaData.isRemote()) {

                    log.debugf("Deploying '%s' queue...", metaData.getName());

                    createQueue(
                            metaData.getName(),
                            metaData.isDurable(),
                            metaData.getSelector(),
                            metaData.isExported()
                    );
                }
            }
        }

        List<TopicMetaData> topicMetaDatas = getUnit().getAttachmentList(TopicMetaData.ATTACHMENTS_KEY);

        if (topicMetaDatas != null && topicMetaDatas.size() > 0) {

            log.debugf("Installing %s topics listed in deployment descriptors...", topicMetaDatas.size());

            for (TopicMetaData metaData : topicMetaDatas) {
                if (!metaData.isRemote()) {

                    log.debugf("Deploying '%s' topic...", metaData.getName());

                    createTopic(
                            metaData.getName(),
                            metaData.isExported()
                    );
                }
            }
        }
    }

    /**
     * Creates a new queue by deploying required services.
     * <p/>
     * This method is executed asynchronously.
     *
     * @param queueName The queue name
     * @param durable   If the queue should be durable
     * @param selector  The optional selector used for the queue
     * @param exported  If the queue should be available in remote JNDI lookups
     * @return CountDownLatch The latch to check if the service is fully started
     * @see DestroyableJMSQueueService
     * @see CountDownLatch
     */
    public CountDownLatch createQueue(final String queueName, final boolean durable, final String selector, boolean exported) {
        if (DestinationUtils.destinationPointerExists(getUnit(), queueName)) {
            log.debugf("Service for '%s' queue already exists", queueName);
            return new CountDownLatch(0);
        }

        DestroyableJMSQueueService queue =
                QueueInstaller.deployGlobalQueue(getUnit().getServiceRegistry(),
                        getGlobalTarget(),
                        queueName,
                        durable,
                        selector,
                        DestinationUtils.jndiNames(queueName, exported));

        createDestinationService(
                queueName,
                QueueInstaller.queueServiceName(queueName),
                queue.getReferenceCount()
        );

        return queue.getStartLatch();
    }

    /**
     * Creates a new topic by deploying required services.
     * <p/>
     * This method is executed asynchronously.
     *
     * @param topicName The name of the topic
     * @param exported  If the topic should be accessible in remote JNDI lookups
     * @return CountDownLatch The latch to check if the service is fully started
     * @see DestroyableJMSTopicService
     * @see CountDownLatch
     */
    public CountDownLatch createTopic(String topicName, boolean exported) {
        if (DestinationUtils.destinationPointerExists(getUnit(), topicName)) {
            log.debugf("Service for '%s' topic already exists", topicName);
            return new CountDownLatch(0);
        }

        DestroyableJMSTopicService topic =
                TopicInstaller.deployGlobalTopic(getUnit().getServiceRegistry(),
                        getGlobalTarget(),
                        topicName,
                        DestinationUtils.jndiNames(topicName, exported));

        createDestinationService(
                topicName,
                TopicInstaller.topicServiceName(topicName),
                topic.getReferenceCount()
        );

        return topic.getStartLatch();
    }

    /**
     * Removes the destination (queue or topic) by undeploying the services.
     * <p/>
     * This method is executed asynchronously.
     *
     * @param name Name of the destination (queue or topic)
     * @return CountDownLatch The latch to check if the service is fully stopped
     * @see CountDownLatch
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public CountDownLatch removeDestination(String name) {
        ServiceName serviceName = this.destinations.get(name);
        if (serviceName != null) {
            ServiceRegistry registry = getUnit().getServiceRegistry();
            ServiceController dest = registry.getService(serviceName);
            if (dest != null) {
                ServiceController globalDest =
                        registry.getService(QueueInstaller.queueServiceName(name));
                if (globalDest == null) {
                    globalDest = registry.getService(TopicInstaller.topicServiceName(name));
                }
                if (globalDest == null) {
                    //should never happen, but...
                    throw new IllegalStateException("Failed to find global dest for " + name);
                }

                Object service = globalDest.getService();
                //force it to destroy, even if it's durable
                if (service instanceof Destroyable) {
                    ((Destroyable) service).setShouldDestroy(true);
                }

                dest.setMode(Mode.REMOVE);

                if (service instanceof HasStartStopLatches) {
                    return ((HasStartStopLatches) service).getStopLatch();
                }
            }
            this.destinations.remove(name);
        }

        // In case the service is already removed or the service is not created by TB, return a dummy CountDownLatch.
        return new CountDownLatch(0);
    }

    protected void createDestinationService(String destName,
                                            ServiceName globalName,
                                            AtomicInteger referenceCount) {
        this.destinations.put(destName,
                DestinationUtils.deployDestinationPointerService(
                        getUnit(),
                        getTarget(),
                        destName,
                        globalName,
                        referenceCount
                ));
    }

    // Useful for testing
    @SuppressWarnings("unused")
    public Map<String, ServiceName> getDestinations() {
        return this.destinations;
    }

    private Map<String, ServiceName> destinations = new HashMap<String, ServiceName>();

    static final Logger log = Logger.getLogger("org.torquebox.messaging");
}
