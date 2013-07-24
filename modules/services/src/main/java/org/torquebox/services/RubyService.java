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

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RestartableRubyRuntimePool;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.core.runtime.RubyRuntimePoolRestartListener;
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.services.component.ServiceComponent;

public class RubyService implements RubyServiceMBean, RubyRuntimePoolRestartListener {

    public RubyService(String name) {
        this.name = name;
    }

    public synchronized void create() throws Exception {
        this.runtimePool.registerRestartListener( this );
        this.runtime = this.runtimePool.borrowRuntime( this.resolver.getComponentName() );
        this.servicesComponent = (ServiceComponent) this.resolver.resolve( runtime );
    }

    public synchronized void start() {
        if (!isStarted()) {
            this.servicesComponent.start();
            this.started = true;
        }
    }

    public synchronized void stop() {
        if (this.servicesComponent != null) {
            this.servicesComponent.stop();
        }
        this.started = false;
    }

    public synchronized void destroy() {
        this.servicesComponent = null;
        if (this.runtime != null) {
            try {
                RuntimeHelper.evalScriptlet( this.runtime, "ActiveRecord::Base.clear_active_connections! if defined?(ActiveRecord::Base)" );
            } finally {
                this.runtimePool.returnRuntime( this.runtime );
            }
        }
    }

    public synchronized void runtimeRestarted() {
        boolean created = this.runtime != null;
        boolean started = isStarted();
        try {
            if (started) {
                log.info( "Restarting service " + this + " after runtime restart" );
                this.stop();
            }
            if (created) {
                this.destroy();
                this.create();
            }
            if (started) {
                this.start();
            }
        } catch (Exception e) {
            log.error( "Error restarting service " + this, e );
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

    public void setRubyRuntimePool(RestartableRubyRuntimePool runtimePool) {
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
    @SuppressWarnings("unused")
    public String inspect() {
        return this.toString();
    }


    private String name;
    private boolean started;

    private ComponentResolver resolver;
    private RestartableRubyRuntimePool runtimePool;
    private Ruby runtime;
    private ServiceComponent servicesComponent;
    private static final Logger log = Logger.getLogger( "org.torquebox.services" );

}
