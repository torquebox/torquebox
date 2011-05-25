package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.StompServer;
import org.torquebox.messaging.metadata.StompMetaData;

public class StompProcessorDeployer extends AbstractDeployer {

	public StompProcessorDeployer() {
		addInput( StompMetaData.class );
		addOutput( BeanMetaData.class );
		setStage( DeploymentStages.REAL );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends StompMetaData> allMetaData = unit.getAllMetaData( StompMetaData.class );
		if (allMetaData.isEmpty())
			return;
		StompMetaData metaData = allMetaData.iterator().next();
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( "torquebox.StompServer",
				StompServer.class.getName() );
		builder.addPropertyMetaData( "port", metaData.getPort() );
		builder.addPropertyMetaData( "server", builder.createInject( "JMSServerManager" ) );

		builder.addInstall( "onInstall" );
		builder.addUninstall( "onUninstall" );
		AttachmentUtils.attach( unit, builder.getBeanMetaData() );

	}

}
