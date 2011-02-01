package org.torquebox.messaging.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.QueueMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: queues.yml
 *   Out: QueueMetaData
 * </pre>
 * 
 * Creates QueueMetaData instances from queues.yml
 */
public class QueuesYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public QueuesYamlParsingDeployer() {
        setSectionName("queues");
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object baseData) throws Exception {
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) baseData;

        for (String queueName : data.keySet()) {
            QueueMetaData queueMetaData = new QueueMetaData(queueName);
            AttachmentUtils.multipleAttach(unit, queueMetaData, queueName);
        }
    }

}
