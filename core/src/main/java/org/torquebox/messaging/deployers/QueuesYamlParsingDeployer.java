package org.torquebox.messaging.deployers;

import java.io.InputStream;
import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.QueuesMetaData;
import org.yaml.snakeyaml.Yaml;

public class QueuesYamlParsingDeployer extends AbstractVFSParsingDeployer<QueuesMetaData>{

	public QueuesYamlParsingDeployer() {
		super( QueuesMetaData.class );
		setName( "queues.yml" );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected QueuesMetaData parse(VFSDeploymentUnit unit, VirtualFile file, QueuesMetaData root) throws Exception {
		InputStream in = null;
		
		QueuesMetaData queues = unit.getAttachment( QueuesMetaData.class );
		if ( queues == null ) {
			queues = new QueuesMetaData();
			unit.addAttachment( QueuesMetaData.class, queues );
		}
		
		try {
			in = file.openStream();
			Yaml yaml = new Yaml();
			Map<String, Map<String,Object>> data = (Map<String, Map<String, Object>>) yaml.load( in );
			
			for ( String queueName : data.keySet() ) {
				log.info( "Read configuration for queue [" + queueName + "]" );
				QueueMetaData queue = new QueueMetaData( queueName );
				queues.addQueue( queue );
			}
		} finally {
			if ( in != null ) {
				in.close();
			}
		}
		return null;
	}

}
