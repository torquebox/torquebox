/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.naming.remote.client.InitialContextFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Properties;

public class RemoteMessageProcessorGroup extends MessageProcessorGroup {

    private static String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";

    public RemoteMessageProcessorGroup(ServiceRegistry registry, ServiceName baseServiceName, String destinationName, String remoteHost, String username, String password) {
        super(registry, baseServiceName, destinationName);

        this.remoteHost = remoteHost;
        this.username = username;
        this.password = password;
    }

    @Override
    public void start(final StartContext context) throws StartException {

        final boolean async = this.startAsynchronously;

        Runnable action = new Runnable() {

            @Override
            public void run() {
                startConnection(context);

                try {
                    start();
                } catch (Exception e) {
                    context.failed(new StartException(e));
                }

                if (async) {
                    context.complete();
                }
            }
        };

        if (async) {
            context.asynchronous();
            context.execute(action);
        } else {
            action.run();
        }

    }

    @Override
    public void stop(StopContext context) {
        super.stop(context);

        try {
            this.connectionFactory.close();
            this.namingContext.close();
        } catch (NamingException e) {
            log.error("Couldn't close connection or connection factory", e);
        }
    }

    /**
     * Prepares a remote connection
     *
     * Lookups connection factory, obtains the destination, creates a connection
     * and starts the connection.
     */
    @Override
    protected void startConnection(StartContext context) {
        log.trace("Initializing remote connection");

        try {
            try {
                namingContext = new InitialContext(prepareProperties());
                connectionFactory = (HornetQConnectionFactory) namingContext.lookup(CONNECTION_FACTORY);
            } catch (NameNotFoundException e) {
                context.failed(new StartException("Could not lookup remote connection factory; connection factory name: " + CONNECTION_FACTORY + " not found on remote host", e));
            }

            try {
                destination = (Destination) namingContext.lookup(getDestinationName());
            } catch (NameNotFoundException e) {
                // Skipping the exception here, we know what's going on
                context.failed(new StartException("Could not lookup destination (" + destinationName + "); make sure it is exported in the 'jboss/exported' naming context as 'jboss/exported" + destinationName + "' on the remote host; read more about it: https://docs.jboss.org/author/display/AS71/JNDI+Reference"));
            }
        } catch (NamingException e) {
            context.failed(new StartException("Could not lookup remote connection factory; make sure you: 1. specified correct remote host, 2. added the application user on remote host, 3. specified correct credentials for added user in the queue section of your deployment descriptor", e));
        }

        try {
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            context.failed(new StartException("Could not create or start remote JMS connection", e));
        }

        log.trace("Remote connection initialized");
    }

    /**
     * Prepares properties to be used to connect to remote host
     *
     * @return {@link Properties}
     */
    private Properties prepareProperties() {
        log.trace("Preparing properties to use to connect to remote host");

        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactory.class.getName());
        // Host is required
        env.put(Context.PROVIDER_URL, "remote://" + remoteHost);

        // Everything else is optional
        if (username != null)
            env.put(Context.SECURITY_PRINCIPAL, username);

        if (password != null)
            env.put(Context.SECURITY_CREDENTIALS, password);

        log.trace("Remoting properties:" + env);

        return env;
    }

    private Context namingContext;
    private HornetQConnectionFactory connectionFactory;
    private String remoteHost;
    private String username;
    private String password;
}
