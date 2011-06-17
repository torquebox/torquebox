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

package org.torquebox.web.websockets;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <code>Service</code> wrapper for <code>WebSocketServer</code>.
 * 
 * <p>
 * This service is typically stood up as a singleton within the AS.
 * </p>
 * 
 * @see WebSocketsServer
 * @see WebSocketsServices#WEB_SOCKETS_SERVER
 * 
 * @author Bob McWhirter
 */
public class WebSocketsServerService implements Service<WebSocketsServer> {

    @Override
    public WebSocketsServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        log.info( "Starting WebSocketsServer Service" );
        this.server = new WebSocketsServer( 8081 );
        // this.server.setExecutor( this.executorInjector.getValue() );
        // FIXME: Use domain model to acquire AS-managed Executor
        this.server.setExecutor( Executors.newFixedThreadPool( 10 ) );
        context.asynchronous();
        context.execute( new Runnable() {
            public void run() {
                server.start();
                context.complete();
            }
        } );

    }

    @Override
    public void stop(StopContext context) {
        this.server.stop();
        this.server = null;
    }

    Injector<Executor> getExecutorInjector() {
        return this.executorInjector;
    }

    private WebSocketsServer server;

    private InjectedValue<Executor> executorInjector = new InjectedValue<Executor>();

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
}
