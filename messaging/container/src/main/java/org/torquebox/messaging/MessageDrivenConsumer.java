package org.torquebox.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.torquebox.common.util.StringUtils;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class MessageDrivenConsumer implements MessageListener {
	
	private static final Logger log = Logger.getLogger( MessageDrivenConsumer.class );
	
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	private Destination destination;
	private ConnectionFactory connectionFactory;
	private Session session;
	private MessageConsumer consumer;
	
	private RubyRuntimePool rubyRuntimePool;
	
	private Connection connection;
	
	private String rubyClassName;
	

	public MessageDrivenConsumer() {
		
	}
	
	public String toString() {
		return "[MessageDrivenConsumer: rubyClassName=" + this.rubyClassName + "]";
	}
	
	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}
	
	public String getRubyClassName() {
		return this.rubyClassName;
	}
	
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
	public Destination getDestination() {
		return this.destination;
	}
	
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}
	
	public void setRubyRuntimePool(RubyRuntimePool rubyRuntimePool) {
		this.rubyRuntimePool = rubyRuntimePool;
	}
	
	public RubyRuntimePool getRubyRuntimePool() {
		return this.rubyRuntimePool;
	}
	
	public void create() throws JMSException {
		this.connection = this.connectionFactory.createConnection();
		
		this.session = this.connection.createSession( true, Session.AUTO_ACKNOWLEDGE );
		
		this.consumer = session.createConsumer( getDestination() );
		this.consumer.setMessageListener( this );
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
	
	@Override
	public void onMessage(Message message) {
		log.info( "onMessage()" );
		Ruby ruby = null;
		
		try {
			ruby = getRubyRuntimePool().borrowRuntime();
			String location = StringUtils.underscore( getRubyClassName() ) + ".rb";
			ruby.evalScriptlet( "require %(torquebox/messaging/dispatcher)\n" );
			RubyModule dispatcher = (RubyModule) ruby.getClassFromPath( "TorqueBox::Messaging::Dispatcher" );
			JavaEmbedUtils.invokeMethod( ruby, dispatcher, "dispatch", new Object[] { getRubyClassName(), location, session, message }, void.class);
			message.acknowledge();
		} catch (Exception e) {
			log.error( "unable to dispatch", e );
			e.printStackTrace();
		} finally {
			if ( ruby != null ) {
				getRubyRuntimePool().returnRuntime( ruby );
			}
			
			try {
				this.session.commit();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
