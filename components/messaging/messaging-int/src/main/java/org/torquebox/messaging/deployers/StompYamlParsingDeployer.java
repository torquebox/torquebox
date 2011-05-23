package org.torquebox.messaging.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.StompMetaData;

public class StompYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

	public StompYamlParsingDeployer() {
		setSectionName( "stomp" );
		addOutput( StompMetaData.class );
		setRelativeOrder( 10000 );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void parse(VFSDeploymentUnit unit, Object baseData) throws Exception {
		log.info( "Parsing stomp information." );
		Map<String, Object> data = (Map<String, Object>) baseData;
		StompMetaData stompMetaData = new StompMetaData();
		if (data.containsKey( "port" ))
			stompMetaData.setPort( (Integer) data.get( "port" ) );
		AttachmentUtils.multipleAttach( unit, stompMetaData, "stomp-" + stompMetaData.getPort() );
		log.info( "Done parsing stomp information." );
	}

}
