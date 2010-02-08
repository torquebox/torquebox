package org.torquebox.enterprise.ruby.messaging.container;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.torquebox.enterprise.ruby.messaging.MessageDrivenAgent;
import org.torquebox.enterprise.ruby.messaging.MessageHandler;
import org.torquebox.enterprise.ruby.messaging.metadata.MessageDrivenAgentConfig;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class Container {

	private Context context;
	private List<MessageDrivenAgent> agents = new ArrayList<MessageDrivenAgent>();
	
	private ConnectionFactory connectionFactory;
	private String connectionFactoryJndiName;
	private RubyRuntimePool rubyRuntimePool;

	public Container() {

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
	
	synchronized void addMessageDrivenAgent(MessageDrivenAgent agent) {
		this.agents.add(agent);
	}
	
	public void addMessageDrivenAgent(MessageDrivenAgentConfig agentConfig) throws NamingException {
		MessageDrivenAgent agent = new MessageDrivenAgent();
		
		agent.setConnectionFactory( getConnectionFactory() );
		
		MessageHandler messageHandler = new MessageHandler();
		messageHandler.setRubyClassName( agentConfig.getRubyClassName() );
		messageHandler.setRubyRuntimePool( getRubyRuntimePool() );
		agent.setMessageHandler( messageHandler );
		
		Destination destination = (Destination) getContext().lookup( agentConfig.getDestinationName() );
		agent.setDestination( destination );
		
		addMessageDrivenAgent( agent );
	}
	
	public void create() throws Exception {
		this.connectionFactory = (ConnectionFactory) getContext().lookup( getConnectionFactoryJndiName() );
	}

	synchronized public void start() {
		for ( MessageDrivenAgent agent : this.agents ) {
			try {
				agent.setConnectionFactory( getConnectionFactory() );
				agent.start();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized public void stop() {
		for ( MessageDrivenAgent agent : this.agents ) {
			try {
				agent.stop();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

}
