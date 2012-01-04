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

import org.jboss.as.network.SocketBinding;
import org.jboss.as.web.VirtualHost;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class StompEndpointBindingService implements Service<String> {

    public StompEndpointBindingService(String hostName, String context) {
        this.hostName = hostName;
        this.context = context;
    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        return this.binding.toString();
    }

    @Override
    public void start(StartContext context) throws StartException {
        int port = socketBindingInjector.getValue().getSocketAddress().getPort();

        String host = this.hostName;

        if (host == null) {
            VirtualHost vhost = this.virtualHostInjector.getOptionalValue();

            if (vhost != null) {
                host = vhost.getHost().getName();
                if (host.equals( "default-host" )) {
                    if (vhost.getHost().getName() != null) {
                        host = vhost.getHost().getName();
                    } else {
                        String[] aliases = vhost.getHost().findAliases();
                        if (aliases != null && aliases.length >= 1) {
                            host = aliases[0];
                        }
                    }
                }
            }

        }

        if (host == null) {
            host = socketBindingInjector.getValue().getAddress().getHostAddress();
        }
        
        this.binding = new StompEndpointBinding( host, port, this.context );

        log.info( "Advertising STOMP binding: " + this.binding );
    }

    @Override
    public void stop(StopContext context) {
        this.binding = null;
    }

    public Injector<SocketBinding> getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    public Injector<VirtualHost> getVirtualHostInjector() {
        return this.virtualHostInjector;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.stomp.binding" );

    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<SocketBinding>();
    private InjectedValue<VirtualHost> virtualHostInjector = new InjectedValue<VirtualHost>();

    private StompEndpointBinding binding;
    private String hostName;
    private String context;

}
