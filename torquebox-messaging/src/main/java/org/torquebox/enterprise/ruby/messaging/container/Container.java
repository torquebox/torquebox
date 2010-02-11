package org.torquebox.enterprise.ruby.messaging.container;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.torquebox.enterprise.ruby.messaging.MessageDrivenConsumer;
import org.torquebox.enterprise.ruby.messaging.metadata.MessageDrivenAgentConfig;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

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

	synchronized void addMessageDrivenAgent(MessageDrivenConsumer agent) {
		this.consumers.add(agent);
	}

	public void addMessageDrivenAgent(MessageDrivenAgentConfig agentConfig) throws NamingException {
		MessageDrivenConsumer consumer = new MessageDrivenConsumer();

		consumer.setConnectionFactory(getConnectionFactory());

		consumer.setRubyClassName(agentConfig.getRubyClassName());
		consumer.setRubyRuntimePool(getRubyRuntimePool());

		Destination destination = (Destination) getContext().lookup(agentConfig.getDestinationName());
		consumer.setDestination(destination);

		addMessageDrivenAgent(consumer);
	}

	public void create() throws Exception {
		this.connectionFactory = (ConnectionFactory) getContext().lookup(getConnectionFactoryJndiName());
		for (MessageDrivenConsumer agent : this.consumers) {
			try {
				agent.setConnectionFactory(getConnectionFactory());
				agent.setRubyRuntimePool( getRubyRuntimePool() );
				agent.create();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized public void start() {
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				System.err.println( "starting consumer: " + consumer );
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
	}
	
	synchronized public void destroy() {
		for (MessageDrivenConsumer consumer : this.consumers) {
			try {
				consumer.destroy();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

}
