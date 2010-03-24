package org.torquebox.messaging.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.messaging.metadata.TopicMetaData;
import org.torquebox.messaging.metadata.TopicsMetaData;
import org.yaml.snakeyaml.Yaml;

public class TopicsYamlParsingDeployer extends AbstractVFSParsingDeployer<TopicsMetaData>{

	public TopicsYamlParsingDeployer() {
		super( TopicsMetaData.class );
		setName( "topics.yml" );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TopicsMetaData parse(VFSDeploymentUnit unit, VirtualFile file, TopicsMetaData root) throws Exception {
		InputStream in = null;
		
		TopicsMetaData topics = unit.getAttachment( TopicsMetaData.class );
		if ( topics == null ) {
			topics = new TopicsMetaData();
			unit.addAttachment( TopicsMetaData.class, topics );
		}
		
		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Map<String,Object>> data = (Map<String, Map<String, Object>>) yaml.load( in );
			
			for ( String topicName : data.keySet() ) {
				log.info( "Read configuration for topic [" + topicName + "]" );
				TopicMetaData topic = new TopicMetaData( topicName );
				topics.addTopic( topic );
			}
		} finally {
			if ( in != null ) {
				in.close();
			}
		}
		return null;
	}

}
