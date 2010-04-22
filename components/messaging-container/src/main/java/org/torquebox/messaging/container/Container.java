package org.torquebox.messaging.container;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.messaging.core.MessageDrivenConsumer;
import org.torquebox.messaging.metadata.MessageDrivenConsumerConfig;

public class Container {

	private Context context;
	private List<MessageDrivenConsumer> consumers = new ArrayList<MessageDrivenConsumer>();

	private ConnectionFactory connectionFactory;
	private String connectionFactoryJndiName;
	private RubyRuntimePool rubyRuntimePool;

	public Container() {

	}

	public String toString() {
		return "[Container: context=" + this.context + "; connectionFactoryJndiName=" + this.connectionFactoryJndiName
				+ "]";
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return this.context;
	}

	public void setConnectionFactoryJndiName(String connectionFactoryName) {
		this.connectionFactoryJndiName = connectionFactoryName;
	}

	public String getConnectionFactoryJndiName() {
		return this.connectionFactoryJndiName;
	}

	protected ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	public void setRubyRuntimePool(RubyRuntimePool rubyRuntimePool) {
		this.rubyRuntimePool = rubyRuntimePool;
	}

	public RubyRuntimePool getRubyRuntimePool() {
		return this.rubyRuntimePool;
	}

	synchronized void addMessageDrivenConsumer(MessageDrivenConsumer consumer) {
		this.consumers.add(consumer);
	}

	public void addMessageDrivenConsumer(MessageDrivenConsumerConfig consumerConfig) throws NamingException {
		MessageDrivenConsumer consumer = new MessageDrivenConsumer();

		consumer.setConnectionFactory(getConnectionFactory());

		consumer.setRubyClassName(consumerConfig.getRubyClassName());
		consumer.setRubyRuntimePool(getRubyRuntimePool());

		Destination destination = (Destination) getContext().lookup(consumerConfig.getDestinationName());
		consumer.setDestination(destination);

		addMessageDrivenConsumer( consumer );
	}

	synchronized public void create() throws Exception {
		this.connectionFactory = (ConnectionFactory) getContext().lookup(getConnectionFactoryJndiName());
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				consumer.setConnectionFactory(getConnectionFactory());
				consumer.setRubyRuntimePool( getRubyRuntimePool() );
				consumer.create();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized public void start() {
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				consumer.start();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized public void stop() {
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				consumer.stop();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		destroy();
	}
	
	synchronized private void destroy() {
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				consumer.destroy();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		this.consumers.clear();
	}

}
