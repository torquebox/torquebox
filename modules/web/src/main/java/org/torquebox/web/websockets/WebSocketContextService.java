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

import org.apache.catalina.Context;
import org.jboss.as.web.VirtualHost;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

/**
 * <code>Service</code> for standing up <code>WebSocketContext</code>.
 * 
 * @see WebSocketContext
 * @see WebSocketContextInstaller
 * 
 * @author Bob McWhirter
 */
public class WebSocketContextService implements Service<WebSocketContext> {

    /**
     * Construct.
     * 
     * @param urlRegistry Registry for tracking endpoint URL.
     * @param name Name of the endpoint.
     * @param contextPath Full context-path of the endpoint.
     */
    public WebSocketContextService(URLRegistry urlRegistry, String name, String contextPath) {
        this.urlRegistry = urlRegistry;
        this.name = name;
        this.contextPath = contextPath;
    }

    @Override
    public WebSocketContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.info( "Starting websockets context: " + contextPath );
        log.info( "  server: " + this.serverInjector.getValue() );
        this.context = new WebSocketContext( this.name, this.serverInjector.getValue(), this.hostInjector.getValue(), contextPath );
        this.context.setRubyRuntimePool( this.runtimePoolInjector.getValue() );
        this.context.setComponentResolver( this.resolverInjector.getValue() );
        Context webContext = this.contextInjector.getValue();
        this.context.setManager( webContext.getManager() );
        this.context.start();

        registerURL();
    }

    /**
     * Register this endpoint's URL with the URL registry provided for
     * user-space applications.
     */
    protected void registerURL() {
        String hostName = this.hostInjector.getValue().getHost().getName();
        int port = this.serverInjector.getValue().getPort();

        String url = "ws://" + hostName + ":" + port + this.contextPath;

        this.urlRegistry.registerURL( this.name, url );
    }

    @Override
    public void stop(StopContext context) {
        this.context.stop();
    }

    Injector<WebSocketsServer> getServerInjector() {
        return this.serverInjector;
    }

    Injector<VirtualHost> getHostInjector() {
        return this.hostInjector;
    }

    Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.runtimePoolInjector;
    }

    Injector<ComponentResolver> getComponentResolverInjector() {
        return this.resolverInjector;
    }

    Injector<Context> getContextInjector() {
        return this.contextInjector;
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );

    private InjectedValue<WebSocketsServer> serverInjector = new InjectedValue<WebSocketsServer>();
    private InjectedValue<VirtualHost> hostInjector = new InjectedValue<VirtualHost>();
    private InjectedValue<RubyRuntimePool> runtimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<ComponentResolver> resolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<Context> contextInjector = new InjectedValue<Context>();

    private URLRegistry urlRegistry;
    private String name;
    private String contextPath;

    private WebSocketContext context;

}
