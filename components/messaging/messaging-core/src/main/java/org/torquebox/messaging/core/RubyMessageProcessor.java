/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.messaging.core;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.jboss.logging.Logger;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyMessageProcessor implements RubyMessageProcessorMBean {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( RubyMessageProcessor.class );

    public RubyMessageProcessor() {

    }

    public String toString() {
        return "[RubyMessageProcessor: " + getName() + "]";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setComponentResolver(RubyComponentResolver resolver) {
        this.componentResolver = resolver;
    }

    public RubyComponentResolver getComponentResolver() {
        return this.componentResolver;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Destination getDestination() {
        return this.destination;
    }

    public String getDestinationName() {
        return this.destination.toString();
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public String getMessageSelector() {
        return this.messageSelector;
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

    public void setRubyRuntimePool(RubyRuntimePool rubyRuntimePool) {
        this.rubyRuntimePool = rubyRuntimePool;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.rubyRuntimePool;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getConcurrency() {
        return this.concurrency;
    }

    // Durability only has meaning for topic processors, not for queues
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean getDurable() {
        return this.durable;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void create() throws JMSException {
        if (getConcurrency() > 1 && getDestination() instanceof Topic) {
            log.warn( "Creating " + this.toString() + " for a Topic with a " +
                      "concurrency greater than 1. This will result in " +
                      "duplicate messages being processed and is an uncommon " +
                      "usage of Topic MessageProcessors.");
        }
        this.connection = this.connectionFactory.createConnection();
        this.connection.setClientID( getApplicationName() );
        for (int i = 0; i < getConcurrency(); i++) {
            new Handler( this.connection.createSession( true, this.acknowledgeMode ) );
        }
    }

    public void start() throws JMSException {
        if (connection != null) {
            connection.start();
            this.started = true;
        }
    }

    public void stop() throws JMSException {
        if (this.connection != null) {
            this.connection.stop();
            this.started = false;
        }
    }

    public void destroy() throws JMSException {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }

    public synchronized String getStatus() {
        if ( this.started ) {
            return "STARTED";
        }
        
        return "STOPPED";
    }

    protected IRubyObject instantiateProcessor(Ruby ruby) throws Exception {
        return this.componentResolver.resolve( ruby );
    }

    protected void processMessage(IRubyObject processor, Message message) {
        Ruby ruby = processor.getRuntime();
        JavaEmbedUtils.invokeMethod( ruby, processor, "process!", new Object[] { message }, void.class );
    }

    class Handler implements MessageListener {

        Handler(Session session) throws JMSException {
            MessageConsumer consumer = null;
            if (getDurable() && getDestination() instanceof Topic) {
                consumer = session.createDurableSubscriber( (Topic)getDestination(), 
                                                            getName(),
                                                            getMessageSelector(),
                                                            false );
            } else {
                if (getDurable() && !(getDestination() instanceof Topic)) {
                    log.warn( "Durable set for processor " + getName() + ", but "
                              + getDestinationName() + " is not a topic - ignoring." );
                }
                consumer = session.createConsumer( getDestination(), getMessageSelector() );
            }
            consumer.setMessageListener( this );
            this.session = session;
        }

        public void onMessage(Message message) {
            Ruby ruby = null;

            try {
                ruby = getRubyRuntimePool().borrowRuntime();
                IRubyObject processor = instantiateProcessor( ruby );
                processMessage( processor, message );
                if (session.getTransacted()) {
                    session.commit();
                }
            } catch (Exception e) {
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

        private Session session;
    }

    private String name;
    private Destination destination;
    private String messageSelector;
    private ConnectionFactory connectionFactory;
    private RubyRuntimePool rubyRuntimePool;
    private Connection connection;
    private RubyComponentResolver componentResolver;
    private int concurrency = 1;
    private boolean started = false;
    private boolean durable = false;
    private String applicationName;

    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

}
