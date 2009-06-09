package org.torquebox.ruby.enterprise.queues;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class RubyTaskQueueHandler {

	private static final Logger log = Logger.getLogger(RubyTaskQueueHandler.class);

	private String destinationName;

	private Connection connection;

	private boolean enabled;

	private RubyRuntimePool runtimePool;

	private String queueClassName;

	private String queueClassLocation;

	public RubyTaskQueueHandler() {
		this.enabled = true;
	}

	public void setQueueName(String destination) {
		this.destinationName = destination;
	}

	public String getQueueName() {
		return this.destinationName;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
		this.runtimePool = runtimePool;
	}
	
	public RubyRuntimePool getRubyRuntimePool() {
		return this.runtimePool;
	}
	
	public void setQueueClassName(String queueClassName) {
		this.queueClassName = queueClassName;
	}
	
	public String getQueueClassName() {
		return this.queueClassName;
	}
	
	public void setQueueClassLocation(String queueClassLocation)  {
		this.queueClassLocation = queueClassLocation;
	}
	
	public String getQueueClassLocation() {
		return this.queueClassLocation;
	}

	public void start() throws NamingException, JMSException {
		if (!enabled) {
			return;
		}
		InitialContext jndiContext = new InitialContext();

		// ConnectionFactory connectionFactory = (ConnectionFactory)
		// jndiContext.lookup("java:JmsXA" );
		ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("java:/ConnectionFactory");
		Destination queue = (Destination) jndiContext.lookup("queue/" + destinationName);

		this.connection = connectionFactory.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(queue);

		BaseRubyMessageListener listener = new BaseRubyMessageListener( this.runtimePool, this.queueClassName, this.queueClassLocation );
		consumer.setMessageListener(listener);

		connection.start();
	}

	public void stop() throws JMSException {
		if (this.connection != null) {
			try {
				this.connection.close();
			} finally {
				this.connection = null;
			}
		}
	}

}
