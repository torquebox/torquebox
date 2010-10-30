package org.torquebox.messaging.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;


/**
 * <pre>
 * Stage: PARSE
 *    In: queues.yml
 *   Out: QueueMetaData
 * </pre>
 *
 * Creates QueueMetaData instances from queues.yml
 */
public class QueuesYamlParsingDeployer extends AbstractVFSParsingDeployer<QueueMetaData> {

	public QueuesYamlParsingDeployer() {
		super(QueueMetaData.class);
		setName("queues.yml");
	}

	
	@Override
	protected boolean accepts(VFSDeploymentUnit unit) throws DeploymentException {
		return super.accepts(unit);
	}


	@SuppressWarnings("unchecked")
	@Override
	protected QueueMetaData parse(VFSDeploymentUnit unit, VirtualFile file, QueueMetaData root) throws Exception {
		InputStream in = null;

		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) yaml.load(in);

			if (data != null) {
				for (String queueName : data.keySet()) {
					QueueMetaData queueMetaData = new QueueMetaData(queueName);
					AttachmentUtils.multipleAttach(unit, queueMetaData, queueName );
				}
			}
		} catch (YAMLException e) {
			log.error("Error parsing " + file + ": " + e.getMessage());
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return null;
	}

}
