package org.torquebox.messaging.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.TopicMetaData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class TopicsYamlParsingDeployer extends AbstractVFSParsingDeployer<TopicMetaData> {

	public TopicsYamlParsingDeployer() {
		super(TopicMetaData.class);
		setName("topics.yml");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TopicMetaData parse(VFSDeploymentUnit unit, VirtualFile file, TopicMetaData root) throws Exception {
		InputStream in = null;

		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) yaml.load(in);

			if (data != null) {
				for (String topicName : data.keySet()) {
					log.info("Read configuration for topic [" + topicName + "]");
					TopicMetaData topicMetaData = new TopicMetaData(topicName);
					AttachmentUtils.multipleAttach(unit, topicMetaData, topicName);
				}
			}
		} catch (YAMLException e) {
			log.error("Error parsing pooling.yml: " + e.getMessage() );
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return null;
	}

}
