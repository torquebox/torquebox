package org.torquebox.messaging.deployers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.core.WebSocketsProcessor;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.messaging.metadata.WebSocketMetaData;

public class WebSocketsProcessorDeployer extends AbstractDeployer {

	private static final String WS_QUEUE_PREFIX = "/queues/websockets_";
	private static final String INBOUND_SUFFIX = "_in";
	private static final String OUTBOUND_SUFFIX = "_out";

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

		// first, add the inbound websockets queue for this app.
		QueueMetaData qmdIn = new QueueMetaData();
		qmdIn.setName( WS_QUEUE_PREFIX + unit.getName() + INBOUND_SUFFIX );
		qmdIn.setDurable( false );
		AttachmentUtils.multipleAttach( unit, qmdIn, qmdIn.getName() );

		// next, add the outbound websockets queue for this app.
		QueueMetaData qmdOut = new QueueMetaData();
		qmdOut.setName( WS_QUEUE_PREFIX + unit.getName() + OUTBOUND_SUFFIX );
		qmdOut.setDurable( false );
		AttachmentUtils.multipleAttach( unit, qmdOut, qmdOut.getName() );

		// then, add the message processor for the websockets queue.
		MessageProcessorMetaData mpMetaData = new MessageProcessorMetaData();
		Map<String, Object> config = new HashMap<String, Object>( 1 );
		config.put( "applicationName", unit.getName() );
		mpMetaData.setRubyConfig( config );
		mpMetaData.setRubyClassName( metaData.getHandler() );
		mpMetaData.setDestinationName( qmdIn.getName() );
		mpMetaData.setDurable( false );
		unit.addAttachment( MessageProcessorMetaData.class, mpMetaData );

		// last, create the WebSocketsProcessor instance.
		String beanName = AttachmentUtils.beanName( unit, WebSocketsProcessor.class, metaData.getContext() );
		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder( beanName,
				WebSocketsProcessor.class.getName() );
		builder.addPropertyMetaData( "context", metaData.getContext() );
		builder.addPropertyMetaData( "port", metaData.getPort() );
		builder.addInstall( "onInstall" );
		builder.addUninstall( "onUninstall" );

		ValueMetaData hornetServerInjection = builder.createInject( "JMSServerManager" );
		builder.addPropertyMetaData( "server", hornetServerInjection );

		AttachmentUtils.attach( unit, builder.getBeanMetaData() );

	}

}
