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

package org.torquebox.stomp;

import java.util.Map;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jruby.Ruby;
import org.projectodd.polyglot.core.AsyncService;
import org.projectodd.stilts.stomplet.Stomplet;
import org.projectodd.stilts.stomplet.container.SimpleStompletContainer;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.stomp.component.XAStompletComponent;

public class StompletService extends AsyncService<Stomplet> {

    public StompletService() {

    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Map<String, String> getConfig() {
        return this.config;
    }

    public void setDestinationPattern(String destinationPattern) {
        this.destinationPattern = destinationPattern;
    }

    public String getDestinationPattern() {
        return this.destinationPattern;
    }

    @Override
    public Stomplet getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void startAsync(StartContext context) throws Exception {
        this.runtime = this.poolInjector.getValue().borrowRuntime( getDestinationPattern() );

        try {
            ComponentResolver componentResolver = this.componentResolverInjector.getValue();
            XAStompletComponent stomplet = (XAStompletComponent) componentResolver.resolve( runtime );

            SimpleStompletContainer container = containerInjector.getValue();
            container.addStomplet( this.destinationPattern, stomplet, this.config );
        } catch (Exception e) {
            this.poolInjector.getValue().returnRuntime( this.runtime );
            this.runtime = null;
            throw e;
        }
    }

    @Override
    public void stop(StopContext context) {
        this.poolInjector.getValue().returnRuntime( this.runtime );
        this.runtime = null;
    }

    public Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.poolInjector;
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }

    public Injector<SimpleStompletContainer> getStompletContainerInjector() {
        return this.containerInjector;
    }

    private InjectedValue<RubyRuntimePool> poolInjector = new InjectedValue<RubyRuntimePool>();
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<SimpleStompletContainer> containerInjector = new InjectedValue<SimpleStompletContainer>();

    private Ruby runtime;
    private Map<String, String> config;

    private String destinationPattern;
}
