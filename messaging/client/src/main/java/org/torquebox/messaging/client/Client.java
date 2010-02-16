package org.torquebox.messaging.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

public class Client {

	private Context context;

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;

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
	
	public Connection getConnection() {
		return this.connection;
	}
	
	public Session getSession() {
		return this.session;
	}

	protected void create() throws JMSException {
		this.connection = getConnectionFactory().createConnection();
		this.session = this.connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
	}

	protected void start() throws JMSException {
		if (this.connection != null) {
			this.connection.start();
		}
	}

	protected void stop() throws JMSException {
		if (this.connection != null) {
			this.connection.stop();
		}
	}

	protected void destroy() throws JMSException {
		if (this.connection != null) {
			this.connection.close();
			this.connection = null;
		}
	}

	public void commit() throws JMSException {
		if (this.session == null) {
			throw new IllegalStateException("No session");
		}
		this.session.commit();
	}

	public void rollback() throws JMSException {
		if (this.session == null) {
			throw new IllegalStateException("No session");
		}
		this.session.rollback();
	}

	public void connect() throws JMSException {
		create();
		start();
	}

	public void close() throws JMSException {
		if ( this.session != null ) {
			commit();
		}
		stop();
		destroy();
	}
	
	public Object lookup(String name) throws NamingException {
		return this.context.lookup( name );
	}

}
