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

package org.torquebox.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class MessageProcessorGroup implements Service<MessageProcessorGroup>, MessageProcessorGroupMBean {

    public MessageProcessorGroup(ServiceRegistry registry, ServiceName baseServiceName, ClassLoader classLoader, String destinationName) {
        this.serviceRegistry = registry;
        this.baseServiceName = baseServiceName;
        this.classLoader = classLoader;
        this.destinationName = destinationName;
    }

    @Override
    public MessageProcessorGroup getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public synchronized void start() throws Exception {
        for (ServiceName eachName : this.services) {
            ServiceController<?> each = this.serviceRegistry.getService( eachName );
            each.setMode( Mode.ACTIVE );
        }
        this.running = true;
    }

    public synchronized void stop() throws Exception {
        for (ServiceName eachName : this.services) {
            ServiceController<?> each = this.serviceRegistry.getService( eachName );
            each.setMode( Mode.NEVER );
        }
        this.running = false;
    }

    @Override
    public String getDestinationName() {
        return this.destinationName;
    }

    @Override
    public String getStatus() {
        if (this.running) {
        	return "STARTED";
        }
        return "STOPPED";
    }

    @Override
    public void start(final StartContext context) throws StartException {

        context.asynchronous();
        context.execute( new Runnable() {

            @Override
            public void run() {
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader( MessageProcessorGroup.this.classLoader );
                ManagedReferenceFactory connectionFactoryManagedReferenceFactory = MessageProcessorGroup.this.connectionFactoryInjector.getValue();
                ManagedReference connectionFactoryManagedReference = connectionFactoryManagedReferenceFactory.getReference();
                ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryManagedReference.getInstance();

                try {
                    MessageProcessorGroup.this.connection = connectionFactory.createConnection();
                    MessageProcessorGroup.this.connection.start();
                } catch (JMSException e) {
                    context.failed( new StartException( e ) );
                } finally {
                    Thread.currentThread().setContextClassLoader( originalClassLoader );
                    if (connectionFactoryManagedReference != null) {
                        connectionFactoryManagedReference.release();
                    }
                }

                ManagedReferenceFactory destinationManagedReferenceFactory = MessageProcessorGroup.this.destinationInjector.getValue();
                ManagedReference destinationManagedReference = destinationManagedReferenceFactory.getReference();
                try {
                    MessageProcessorGroup.this.destination = (Destination) destinationManagedReference.getInstance();
                } finally {
                    if (destinationManagedReference != null) {
                        destinationManagedReference.release();
                    }
                }

                ServiceTarget target = context.getChildTarget();

                for (int i = 0; i < MessageProcessorGroup.this.concurrency; ++i) {
                    MessageProcessorService service = new MessageProcessorService( MessageProcessorGroup.this );
                    ServiceName serviceName = baseServiceName.append( "" + i );
                    target.addService( serviceName, service )
                            .install();
                    services.add( serviceName );
                }

                MessageProcessorGroup.this.running = true;
                
                context.complete();

            }

        } );

    }

    @Override
    public void stop(StopContext context) {
        log.info( "Shutting down JMS connection et al: " + connection );
        try {
            this.connection.close();
        } catch (JMSException e) {
            log.error( "Error stopping consumer connection", e );
        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public int getConcurrency() {
        return this.concurrency;
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

    public Injector<ManagedReferenceFactory> getConnectionFactoryInjector() {
        return this.connectionFactoryInjector;
    }

    public Injector<ManagedReferenceFactory> getDestinationInjector() {
        return this.destinationInjector;
    }

    public Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.runtimePoolInjector;
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Destination getDestination() {
        return this.destination;
    }

    public RubyRuntimePool getRubyRuntimePool() {
        return this.runtimePoolInjector.getValue();
    }

    public ComponentResolver getComponentResolver() {
        return this.componentResolverInjector.getValue();
    }

    private ServiceRegistry serviceRegistry;
    private String destinationName;

    private ClassLoader classLoader;
    private Connection connection;
    private Destination destination;

    private ServiceName baseServiceName;

    private String name;
    private String messageSelector;
    private boolean durable;
    private boolean running = false;
    
    private int concurrency;
    private List<ServiceName> services = new ArrayList<ServiceName>();

    private final InjectedValue<ManagedReferenceFactory> connectionFactoryInjector = new InjectedValue<ManagedReferenceFactory>();
    private final InjectedValue<ManagedReferenceFactory> destinationInjector = new InjectedValue<ManagedReferenceFactory>();
    private final InjectedValue<RubyRuntimePool> runtimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private final InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();

    private static final Logger log = Logger.getLogger( "org.torquebox.message" );

}
