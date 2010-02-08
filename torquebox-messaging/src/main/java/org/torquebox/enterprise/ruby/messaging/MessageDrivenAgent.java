package org.torquebox.enterprise.ruby.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class MessageDrivenAgent {

	private Destination destination;
	private ConnectionFactory connectionFactory;
	
	private boolean transacted = true;
	private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
	
	private MessageHandler messageHandler;
	
	private Connection connection;

	public MessageDrivenAgent() {
		
	}
	
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
	public Destination getDestination() {
		return this.destination;
	}
	
	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}
	
	public boolean isTransacted() {
		return this.transacted;
	}
	
	public void setAcknowledgeMode(int acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}
	
	public int getAcknowledgeMode() {
		return this.acknowledgeMode;
	}
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}
	
	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}
	
	public void create() throws JMSException {
		this.connection = this.connectionFactory.createConnection();
		
		Session session = this.connection.createSession( isTransacted(), getAcknowledgeMode() );
		MessageConsumer consumer = session.createConsumer( getDestination() );
		
		consumer.setMessageListener( getMessageHandler() );
	}
		
	public void start() throws JMSException {
		if ( connection != null ) {
			connection.start();
		}
	}
	
	public void stop() throws JMSException {
		if ( connection != null ) {
			this.connection.stop();
		}
	}
	
	public void destroy() throws JMSException {
		if ( connection != null ) {
			this.connection.close();
			this.connection = null;
		}
	}
}
