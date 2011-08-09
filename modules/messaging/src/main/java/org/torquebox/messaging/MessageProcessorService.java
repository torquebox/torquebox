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

    MessageProcessorService(MessageProcessorGroup group) {
        this.group = group;
    }

    @Override
    public void start(final StartContext context) throws StartException {

        context.asynchronous();
        context.execute( new Runnable() {

            @Override
            public void run() {
                try {
                    session = group.getConnection().createSession( false, Session.AUTO_ACKNOWLEDGE );

                    Destination destination = group.getDestination();
                    if (group.isDurable() && destination instanceof Topic) {
                        consumer = session.createDurableSubscriber( (Topic) destination, group.getName(), group.getMessageSelector(), false );
                    } else {
                        if (group.isDurable() && !(destination instanceof Topic)) {
                            log.warn( "Durable set for processor " + group.getName() + ", but " + destination + " is not a topic - ignoring." );
                        }
                        consumer = MessageProcessorService.this.session.createConsumer( destination, group.getMessageSelector() );
                    }
                    consumer.setMessageListener( MessageProcessorService.this );

                    context.complete();
                } catch (JMSException e) {
                    context.failed( new StartException( e ) );
                }
            }
        } );

    }

    @Override
    public void onMessage(Message message) {
        Ruby ruby = null;
        try {
            ruby = group.getRubyRuntimePool().borrowRuntime();
            MessageProcessorComponent component = (MessageProcessorComponent) group.getComponentResolver().resolve( ruby );
            component.process( message );
            if (session.getTransacted()) {
                session.commit();
            }
        } catch (Exception e) {
            log.error( "Unexpected error in " + group.getName(), e );
            try {
                if (session.getTransacted()) {
                    session.rollback();
                }
            } catch (JMSException ignored) {
            }
        } finally {
            if (ruby != null) {
                group.getRubyRuntimePool().returnRuntime( ruby );
            }
        }

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
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    // -------

    private static final Logger log = Logger.getLogger( "org.torquebox.message" );

    private MessageProcessorGroup group;
    private Session session;
    private MessageConsumer consumer;
}
