/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.stomp;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.stomplet.container.SimpleStompletContainer;
import org.projectodd.stilts.stomplet.server.StompletServer;

public class StompletContainerService implements Service<SimpleStompletContainer> {

    public StompletContainerService() {
    }

    public void setHosts(List<String> hostNames) {
        this.hostNames = hostNames;
    }
    
    public void addHost(String hostName) {
        this.hostNames.add( hostName );
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.container = new SimpleStompletContainer();
        StompSessionManager sessionManager = this.sessionManagerInjector.getValue();
        try {
            this.container.start();
            StompletServer server = this.serverInjector.getValue();
            if (this.hostNames.isEmpty()) {
                server.setDefaultContainer( this.container );
                server.setDefaultSessionManager( sessionManager );
            } else {
                for (String each : this.hostNames) {
                    server.registerVirtualHost( each, this.container, sessionManager );
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            // context.failed( new StartException( e ) );
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            StompletServer server = this.serverInjector.getValue();
            if ( this.hostNames.isEmpty() ) {
                server.setDefaultContainer( null );
                server.setDefaultSessionManager( null );
            } else {
                for (String each : this.hostNames) {
                    server.unregisterVirtualHost( each );
                }
            }
            this.container.stop();
        } catch (Exception e) {
            // ignore
        }

    }

    @Override
    public SimpleStompletContainer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.container;
    }

    public Injector<StompletServer> getStompletServerInjector() {
        return this.serverInjector;
    }
    
    public Injector<StompSessionManager> getSessionManagerInjector() {
        return this.sessionManagerInjector;
    }

    private InjectedValue<StompletServer> serverInjector = new InjectedValue<StompletServer>();
    private InjectedValue<StompSessionManager> sessionManagerInjector = new InjectedValue<StompSessionManager>();
    private SimpleStompletContainer container;
    private List<String> hostNames = new ArrayList<String>();;

}
