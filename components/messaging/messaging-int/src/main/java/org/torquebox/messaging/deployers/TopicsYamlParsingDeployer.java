package org.torquebox.messaging.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.TopicMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: topics.yml
 *   Out: TopicMetaData
 * </pre>
 * 
 * Creates TopicMetaData instances from topics.yml
 */
public class TopicsYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public TopicsYamlParsingDeployer() {
        setSectionName("topics");
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object baseData) throws Exception {
        System.err.println( "deploy with: " + baseData );
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) baseData;

        for (String topicName : data.keySet()) {
            log.info("Read configuration for topic [" + topicName + "]");
            TopicMetaData topicMetaData = new TopicMetaData(topicName);
            AttachmentUtils.multipleAttach(unit, topicMetaData, topicName);
        }
    }

}
