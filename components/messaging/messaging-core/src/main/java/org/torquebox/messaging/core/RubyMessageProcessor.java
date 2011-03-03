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

import java.util.Collections;
import java.util.Map;

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
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyMessageProcessor implements RubyMessageProcessorMBean {

    private static final Logger log = Logger.getLogger( RubyMessageProcessor.class );

    public RubyMessageProcessor() {

    }

    public String toString() {
        return "[MessageDrivenConsumer: " + getName() + "]";
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

    public void setRubyConfig(Map rubyConfig) {
        if (rubyConfig != null)
            this.rubyConfig = rubyConfig;
    }

    public Map getRubyConfig() {
        return this.rubyConfig;
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

    public void create() throws JMSException {
        log.info( "creating for " + getDestination() );
        this.connection = this.connectionFactory.createConnection();
        for (int i = 0; i < getConcurrency(); i++) {
            new Handler( this.connection.createSession( true, this.acknowledgeMode ) );
        }
    }

    public void start() throws JMSException {
        log.info( "starting for " + getDestination() );
        if (connection != null) {
            connection.start();
        }
    }

    public void stop() throws JMSException {
        log.info( "stopping for " + getDestination() );
        if (this.connection != null) {
            log.info( "stopping connection for " + getDestination() );
            this.connection.stop();
        }
    }

    public void destroy() throws JMSException {
        log.info( "destroying for " + getDestination() );
        if (this.connection != null) {
            log.info( "destroying connection for " + getDestination() );
            this.connection.close();
            this.connection = null;
        }
    }

    protected IRubyObject instantiateProcessor(Ruby ruby) throws Exception {
        return this.componentResolver.resolve( ruby );
    }

    protected void configureProcessor(IRubyObject processor) {
        Ruby ruby = processor.getRuntime();
        ReflectionHelper.callIfPossible( ruby, processor, "configure", new Object[] { getRubyConfig() } );
    }

    protected void processMessage(IRubyObject processor, Message message) {
        Ruby ruby = processor.getRuntime();
        JavaEmbedUtils.invokeMethod( ruby, processor, "process!", new Object[] { message }, void.class );
    }

    class Handler implements MessageListener {

        Handler(Session session) throws JMSException {
            log.info( "creating session handler for " + getDestination() );
            MessageConsumer consumer = session.createConsumer( getDestination(), getMessageSelector() );
            consumer.setMessageListener( this );
            this.session = session;
        }

        public void onMessage(Message message) {
            Ruby ruby = null;

            try {
                log.debug( "Received message: " + message );
                ruby = getRubyRuntimePool().borrowRuntime();
                log.debug( "Got runtime: " + ruby );
                IRubyObject processor = instantiateProcessor( ruby );
                log.debug( "Got processor: " + processor );
                configureProcessor( processor );
                log.debug( "Configured processor: " + processor );
                processMessage( processor, message );
                log.debug( "Message processed" );
                if (session.getTransacted())
                    session.commit();
            } catch (Exception e) {
                log.error( "unable to dispatch", e );
                try {
                    if (session.getTransacted())
                        session.rollback();
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
    private Map rubyConfig = Collections.EMPTY_MAP;

    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

}
