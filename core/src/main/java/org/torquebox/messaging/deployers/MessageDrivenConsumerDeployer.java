package org.torquebox.messaging.deployers;

import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.messaging.MessageDrivenConsumer;
import org.torquebox.messaging.MessageDrivenConsumerConfig;
import org.torquebox.messaging.metadata.QueuesMetaData;
import org.torquebox.messaging.metadata.TopicsMetaData;
import org.torquebox.microcontainer.JndiRefMetaData;
import org.torquebox.ruby.core.runtime.deployers.PoolingDeployer;

public class MessageDrivenConsumerDeployer extends AbstractDeployer {

	public MessageDrivenConsumerDeployer() {
		setStage(DeploymentStages.REAL);
		addInput(MessageDrivenConsumerConfig.class);
		addOutput(BeanMetaData.class);
		setRelativeOrder( 1000 );
	}

	@Override
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		Set<? extends MessageDrivenConsumerConfig> consumerConfigs = unit
				.getAllMetaData(MessageDrivenConsumerConfig.class);

		for (MessageDrivenConsumerConfig consumerConfig : consumerConfigs) {
			try {
				deploy(unit, consumerConfig);
			} catch (NamingException e) {
				throw new DeploymentException( e );
			}
		}
	}

	protected void deploy(DeploymentUnit unit,
			MessageDrivenConsumerConfig consumerConfig) throws NamingException {
		String beanName = "message-driven." + consumerConfig.getRubyClassName()
				+ "." + consumerConfig.getDestinationName();

		BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(
				beanName, MessageDrivenConsumer.class.getName());

		ValueMetaData runtimePoolInject = builder.createInject(PoolingDeployer
				.getBeanName(unit, "messaging"));

		builder.addPropertyMetaData("rubyRuntimePool", runtimePoolInject);
		builder.addPropertyMetaData("rubyClassName", consumerConfig
				.getRubyClassName());

		
		if ( demandDestination( unit, consumerConfig.getDestinationName() ) ) {
			String destinationBeanName = AbstractDestinationDeployer.getBeanName( consumerConfig.getDestinationName() );
			builder.addDemand( destinationBeanName, ControllerState.START, ControllerState.INSTALLED, null );
		}
		
		
		Context context = new InitialContext();
		
		JndiRefMetaData destinationJndiRef = new JndiRefMetaData( context, consumerConfig.getDestinationName() );
		builder.addPropertyMetaData("destination", destinationJndiRef );
		
		JndiRefMetaData connectionFactoryJndiRef = new JndiRefMetaData( context, "/ConnectionFactory" );
		builder.addPropertyMetaData("connectionFactory", connectionFactoryJndiRef);
		
		BeanMetaData beanMetaData = builder.getBeanMetaData();
		
		unit.addAttachment(BeanMetaData.class.getName() + "$" + beanName, beanMetaData, BeanMetaData.class);
	}
	
	protected boolean demandDestination(DeploymentUnit unit, String destinationName) {
		
		QueuesMetaData queues = unit.getAttachment(QueuesMetaData.class);
		
		if ( ( queues != null ) && ( queues.getDestination( destinationName ) != null ) ) {
			return true;
		}
		
		TopicsMetaData topics = unit.getAttachment( TopicsMetaData.class );
		
		if ( ( topics != null ) && ( topics.getDestination( destinationName ) != null ) ) {
			return true;
		}
		
		return false;
	}

}
