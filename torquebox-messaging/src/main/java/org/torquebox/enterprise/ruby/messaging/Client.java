package org.torquebox.enterprise.ruby.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

public class Client {

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Context context;

	public Client() {

	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return this.context;
	}
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	public void create() throws JMSException {
		this.connection = getConnectionFactory().createConnection();
	}

	public void start() throws JMSException {
		if ( this.connection != null ) {
			this.connection.start();
		}
	}

	public void stop() throws JMSException {
		if ( this.connection != null ) {
			this.connection.stop();
		}
	}

	public void destroy() throws JMSException {
		if (this.connection != null) {
			this.connection.close();
			this.connection = null;
		}
	}

	protected void send(Destination destination, Object message) throws JMSException {
		System.err.println("sending " + message + " to " + destination);
		Session session = this.connection.createSession(true, Session.AUTO_ACKNOWLEDGE );
		MessageProducer producer = session.createProducer( destination );
		producer.send( session.createTextMessage( message.toString() ) );
		session.commit();
		session.close();
		System.err.println( "committed session" );
	}
	
	protected void send(String destinationName, Object message) throws NamingException, JMSException {
		Destination destination = (Destination) getContext().lookup( destinationName );
		send(destination, message);
	}

}
