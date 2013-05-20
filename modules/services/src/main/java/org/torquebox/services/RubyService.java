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

package org.torquebox.services;

import org.jruby.Ruby;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.services.component.ServiceComponent;

public class RubyService implements RubyServiceMBean {

    public RubyService(String name) {
        this.name = name;
    }

    public void create() throws Exception {
        this.runtime = this.runtimePool.borrowRuntime( this.resolver.getComponentName() );
        this.servicesComponent = (ServiceComponent) this.resolver.resolve( runtime );
    }

    public void start() {
        if (!isStarted()) {
            this.servicesComponent.start();
            this.started = true;
        }
    }

    public void stop() {
        if (this.servicesComponent != null) {
            this.servicesComponent.stop();
        }
        this.started = false;
    }

    public void destroy() {
        if (this.runtime != null) {
            this.runtimePool.returnRuntime( runtime );
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isStarted() {
        return this.started;
    }

    @Override
    public boolean isStopped() {
        return !isStarted();
    }

    @Override
    public String getRubyClassName() {
        return resolver.getComponentInstantiator().toString();
    }

    @Override
    public String getStatus() {
        if (isStarted()) {
            return "STARTED";
        }
        return "STOPPED";
    }

    public void setComponentResolver(ComponentResolver resolver) {
        this.resolver = resolver;
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public ServiceComponent getComponent() {
        return this.servicesComponent;
    }

    @Override
    public String toString() {
        return "[RubyService: name=" + this.name +
                "; status=" + this.getStatus() + "]";
    }

    // Workaround for TORQUE-1075 - Use service name in :inspect implementation
    // https://issues.jboss.org/browse/TORQUE-1075
    public String inspect() {
        return this.toString();
    }

    private String name;
    private boolean started;

    private ComponentResolver resolver;
    private RubyRuntimePool runtimePool;
    private Ruby runtime;
    private ServiceComponent servicesComponent;

}
