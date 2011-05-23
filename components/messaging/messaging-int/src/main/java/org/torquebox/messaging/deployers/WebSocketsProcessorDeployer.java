package org.torquebox.messaging.deployers;

import java.util.Set;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.common.util.StringUtils;
import org.torquebox.injection.AbstractRubyComponentDeployer;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.RubyUpstreamHandler;
import org.torquebox.messaging.core.WebSocketsServer;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.WebSocketMetaData;

public class WebSocketsProcessorDeployer extends AbstractRubyComponentDeployer {

	public WebSocketsProcessorDeployer() {
		addInput( WebSocketMetaData.class );
		addOutput( QueueMetaData.class );
		addOutput( MessageProcessorMetaData.class );
		addOutput( BeanMetaData.class );
		setStage( DeploymentStages.REAL );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {

		Set<? extends WebSocketMetaData> allMetaData = unit.getAllMetaData( WebSocketMetaData.class );
		if (allMetaData.isEmpty())
			return;
		WebSocketMetaData metaData = allMetaData.iterator().next();
		if (StringUtils.isBlank( metaData.getHandler() ))
			throw new DeploymentException(
					"Cannot deploy websocket server with no handler. Please define a proper handler your knob yml." );

		// first, create the websockets server for this node. This is shared.
		String wsServerBeanName = "torquebox.WebSocketsServer";
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( wsServerBeanName,
				WebSocketsServer.class.getName() );
		builder.addPropertyMetaData( "context", metaData.getContext() );
		builder.addPropertyMetaData( "port", metaData.getPort() );
		builder.addInstall( "onInstall" );
		builder.addUninstall( "onUninstall" );
		AttachmentUtils.attach( unit, builder.getBeanMetaData() );

		// then, create the upstream handler for this node. This is
		// app-specific.
		String rhBeanName = AttachmentUtils.beanName( unit, RubyUpstreamHandler.class );
		BeanMetaDataBuilder rhBuilder = BeanMetaDataBuilderFactory.createBuilder( rhBeanName,
				RubyUpstreamHandler.class.getName() );
		String runtimePoolName = AttachmentUtils.beanName( unit, RubyRuntimePool.class, "messaging" );
		rhBuilder.addPropertyMetaData( "rubyRuntimePool", rhBuilder.createInject( runtimePoolName ) );
		BeanMetaData componentResolver = createComponentResolver( unit, "websockets-resolver." + metaData.getHandler(),
				metaData.getHandler(), metaData.getRubyRequirePath(), metaData.getRubyConfig() );
		rhBuilder.addPropertyMetaData( "componentResolver", rhBuilder.createInject( componentResolver.getName() ) );
		rhBuilder.addPropertyMetaData( "webSocketsServer", rhBuilder.createInject( wsServerBeanName ) );
		rhBuilder.addPropertyMetaData( "unitName", unit.getName() );
		rhBuilder.addInstall( "onInstall" );
		AttachmentUtils.attach( unit, rhBuilder.getBeanMetaData() );

	}

}
