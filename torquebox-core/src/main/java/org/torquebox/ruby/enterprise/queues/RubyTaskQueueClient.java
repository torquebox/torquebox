package org.torquebox.ruby.enterprise.queues;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.ruby.enterprise.client.RubyClient;

public class RubyTaskQueueClient {

	private static final Logger log = Logger.getLogger(RubyTaskQueueClient.class);

	private String destinationName;

	private InetSocketAddress namingAddress;
	
	public RubyTaskQueueClient() {
		RubyClient currentClient = RubyClient.getClientForCurrentThread();
		if ( currentClient != null ) {
			this.namingAddress = currentClient.getNamingAddress();
		}
	}
	
	public RubyTaskQueueClient(String namingHost) {
		this( namingHost, 1099 );
	}
	
	public RubyTaskQueueClient(String namingHost, int namingPort) {
		this.namingAddress = new InetSocketAddress( namingHost, namingPort );
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public void enqueue(String taskName, Object payload) throws NamingException, JMSException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		
		if ( this.namingAddress != null ) {
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
			String namingUrl = "jnp://" + this.namingAddress.getHostName() + ":" + this.namingAddress.getPort();
			env.put(Context.PROVIDER_URL, namingUrl );
			env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		}
		
		InitialContext jndiContext = new InitialContext(env);
		
		ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("java:/ConnectionFactory");
		Destination destination = (Destination) jndiContext.lookup("queue/" + destinationName);

		log.info("using destination: " + destination);

		log.info("connection factory: " + connectionFactory);

		Connection connection = connectionFactory.createConnection();
		try {
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			MessageProducer producer = session.createProducer(destination);

			ObjectMessage message = session.createObjectMessage();
			message.setStringProperty("TaskName", taskName);

			log.info("sending payload: " + payload);
			log.info("sending message: " + message);

			if (payload instanceof IRubyObject) {
				IRubyObject rubyPayload = (IRubyObject) payload;
				Ruby ruby = rubyPayload.getRuntime();
				RubyModule marshal = ruby.getClassFromPath("Marshal");
				String marshalled = (String) JavaEmbedUtils.invokeMethod(ruby, marshal, "dump", new Object[] { rubyPayload },
						String.class);
				log.info("marshalled to [" + marshalled + "]");
				message.setObject(marshalled);
				message.setBooleanProperty("IsRubyMarshal", true);
			} else if (payload instanceof Serializable) {
				message.setBooleanProperty("IsRubyMarshal", false);
				message.setObject((Serializable) payload);
			}

			producer.send(message);
		} finally {
			if (connection != null) {
				System.err.println( "closing connection" );
				connection.close();
				System.err.println( "closed connection" );
			}
		}
	}

}
