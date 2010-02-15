package org.torquebox.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimePool;

public class MessageDrivenConsumer implements MessageListener {
	
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
		Ruby ruby = null;
		
		try {
			ruby = getRubyRuntimePool().borrowRuntime();
			RubyClass rubyClass = (RubyClass) ruby.getClassFromPath( getRubyClassName() );
			IRubyObject listener = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", EMPTY_OBJECT_ARRAY, IRubyObject.class );
			
			ReflectionHelper.setIfPossible( ruby, listener, "session", this.session );
			
			JavaEmbedUtils.invokeMethod( ruby, listener, "on_message", new Object[] { message }, void.class);
			message.acknowledge();
		} catch (Exception e) {
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
