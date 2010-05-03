package org.torquebox.messaging.core;

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
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyMessageProcessor implements MessageListener {

	private static final Logger log = Logger
			.getLogger(RubyMessageProcessor.class);

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

	private Destination destination;
	private String messageSelector;
	private ConnectionFactory connectionFactory;
	private Session session;
	private MessageConsumer consumer;

	private String rubyClassName;
	private RubyRuntimePool rubyRuntimePool;

	private Connection connection;

	private String rubyRequirePath;

	private InstantiatingRubyComponentResolver componentResolver;

	private String rubyConfig;


	public RubyMessageProcessor() {

	}

	public String toString() {
		return "[MessageDrivenConsumer: rubyClassName=" + this.rubyClassName
				+ "]";
	}

	public void setRubyClassName(String rubyClassName) {
		this.rubyClassName = rubyClassName;
	}

	public String getRubyClassName() {
		return this.rubyClassName;
	}
	
	public void setRubyRequirePath(String rubyRequirePath) {
		this.rubyRequirePath = rubyRequirePath;
	}
	
	public String getRubyRequirePath() {
		return this.rubyRequirePath;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public Destination getDestination() {
		return this.destination;
	}
	
	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}
	
	public String getMessageSelector() {
		return this.messageSelector;
	}
	
	public void setRubyConfig(String rubyConfig) {
		this.rubyConfig = rubyConfig;
	}
	
	public String getRubyConfig() {
		return this.rubyConfig;
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
		log.info("creating for " + getDestination());
		
		this.componentResolver = new InstantiatingRubyComponentResolver();
		this.componentResolver.setRubyClassName( this.rubyClassName );
		this.componentResolver.setRubyRequirePath( this.rubyRequirePath );
		this.componentResolver.setComponentName( "message-processor." + this.rubyClassName );
		
		this.connection = this.connectionFactory.createConnection();

		this.session = this.connection.createSession(true,
				Session.AUTO_ACKNOWLEDGE);

		this.consumer = session.createConsumer(getDestination(), getMessageSelector() );
		this.consumer.setMessageListener(this);
	}

	public void start() throws JMSException {
		log.info("starting for " + getDestination());
		if (connection != null) {
			connection.start();
		}
	}

	public void stop() throws JMSException {
		log.info("stopping for " + getDestination());
		if (this.connection != null) {
			log.info("stopping connection for " + getDestination());
			this.connection.stop();
		}
	}

	public void destroy() throws JMSException {
		log.info("destroying for " + getDestination());
		if (this.connection != null) {
			log.info("destroying connection for " + getDestination());
			this.connection.close();
			this.connection = null;
		}
	}

	@Override
	public void onMessage(Message message) {
		Ruby ruby = null;

		try {
			ruby = getRubyRuntimePool().borrowRuntime();
			IRubyObject rubyProcessor = this.componentResolver.resolve( ruby );
			JavaEmbedUtils.invokeMethod(ruby, rubyProcessor, "on_message", new Object[] { message }, void.class);
			message.acknowledge();
		} catch (Exception e) {
			log.error("unable to dispatch", e);
			e.printStackTrace();
		} finally {
			if (ruby != null) {
				getRubyRuntimePool().returnRuntime(ruby);
			}

			try {
				this.session.commit();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}
