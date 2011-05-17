package org.torquebox.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.messaging.component.MessageProcessorComponent;

public class MessageProcessorService implements Service<Void>, MessageListener {

    public MessageProcessorService(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void start(final StartContext context) throws StartException {

        context.asynchronous();
        context.execute( new Runnable() {

            @Override
            public void run() {
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader( MessageProcessorService.this.classLoader );
                ManagedReferenceFactory connectionFactoryManagedReferenceFactory = MessageProcessorService.this.connectionFactoryInjector.getValue();
                ManagedReference connectionFactoryManagedReference = connectionFactoryManagedReferenceFactory.getReference();
                ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryManagedReference.getInstance();

                ManagedReferenceFactory destinationManagedReferenceFactory = MessageProcessorService.this.destinationInjector.getValue();
                ManagedReference destinationManagedReference = destinationManagedReferenceFactory.getReference();
                Destination destination = (Destination) destinationManagedReference.getInstance();
                
                try {
                    MessageProcessorService.this.connection = connectionFactory.createConnection();
                    MessageProcessorService.this.session = connection.createSession( true, Session.AUTO_ACKNOWLEDGE );

                    if (MessageProcessorService.this.durable && destination instanceof Topic) {
                        MessageProcessorService.this.consumer = MessageProcessorService.this.session.createDurableSubscriber( (Topic) destination,
                                                            getName(),
                                                            getMessageSelector(),
                                                            false );
                    } else {
                        if (MessageProcessorService.this.durable && !(destination instanceof Topic)) {
                            log.warn( "Durable set for processor " + getName() + ", but "
                                    + destination + " is not a topic - ignoring." );
                        }
                        MessageProcessorService.this.consumer = MessageProcessorService.this.session.createConsumer( destination, getMessageSelector() );
                    }
                    MessageProcessorService.this.consumer.setMessageListener( MessageProcessorService.this );

                    log.info( "START MSG PROC ON " + destination );
                    MessageProcessorService.this.connection.start();
                    context.complete();
                } catch (JMSException e) {
                    context.failed( new StartException( e ) );
                } finally {
                    Thread.currentThread().setContextClassLoader( originalClassLoader );
                    if (connectionFactoryManagedReference != null) {
                        connectionFactoryManagedReference.release();
                    }
                    
                    if ( destinationManagedReference != null ) {
                        destinationManagedReference.release();
                    }
                    
                }
            }
        } );

    }

    @Override
    public void onMessage(Message message) {
        log.info(  "onMessage! " + message );
        Ruby ruby = null;
        try {
            ruby = getRubyRuntimePool().borrowRuntime();
            MessageProcessorComponent component = (MessageProcessorComponent) getComponentResolver().resolve( ruby );
            component.process( message );
            if (session.getTransacted()) {
                session.commit();
            }
        } catch (Exception e) {
            log.error( "Unexpected error in " + getName(), e );
            try {
                if (session.getTransacted()) {
                    session.rollback();
                }
            } catch (JMSException ignored) {
            }
        } finally {
            if (ruby != null) {
                getRubyRuntimePool().returnRuntime( ruby );
            }
        }

    }

    protected RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePoolInjector.getValue();
    }

    protected ComponentResolver getComponentResolver() {
        return this.componentResolverInjector.getValue();
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.consumer.close();
        } catch (JMSException e) {
            log.error( "Error closing consumer connection", e );
        }
        try {
            this.session.close();
        } catch (JMSException e) {
            log.error( "Error closing consumer session", e );
        }
        try {
            this.connection.stop();
        } catch (JMSException e) {
            log.error( "Error stopping consumer connection", e );
        }
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.runtimePoolInjector;
    }

    public Injector<ManagedReferenceFactory> getConnectionFactoryInjector() {
        return this.connectionFactoryInjector;
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }

    public Injector<ManagedReferenceFactory> getDestinationInjector() {
        return this.destinationInjector;
    }

    // -------

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public String getMessageSelector() {
        return this.messageSelector;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isDurable() {
        return this.durable;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.message" );

    private ClassLoader classLoader;
    
    private final InjectedValue<RubyRuntimePool> runtimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private final InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private final InjectedValue<ManagedReferenceFactory> connectionFactoryInjector = new InjectedValue<ManagedReferenceFactory>();
    private final InjectedValue<ManagedReferenceFactory> destinationInjector = new InjectedValue<ManagedReferenceFactory>();

    private String name;
    private String messageSelector;
    private boolean durable;

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
}
