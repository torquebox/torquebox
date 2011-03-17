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

package org.torquebox.services;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.injection.spi.InjectableRegistry;
import org.torquebox.injection.spi.RubyInjectionProxy;
import org.torquebox.interp.core.ManagedComponentResolver;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyServiceProxy implements RubyInjectionProxy, RubyServiceProxyMBean {
    
    public RubyServiceProxy() {
        // MicroContainer seems to want this declared
    }

    public RubyServiceProxy(RubyComponentResolver resolver, RubyRuntimePool pool) {
        setRubyComponentResolver( resolver );
        setRubyRuntimePool( pool );
    }

    public void setRubyRuntimePool(RubyRuntimePool runtimePool) {
        this.runtimePool = runtimePool;
    }

    public void setRubyComponentResolver(RubyComponentResolver resolver) {
        this.resolver = resolver;
    }
    
    public String getRubyClassName() {
        if ( this.resolver instanceof ManagedComponentResolver ) {
            return ((ManagedComponentResolver) this.resolver).getComponentName();
        }
        
        return "<unknown>";
    }
    
    public synchronized void create() throws Exception {
        if (this.ruby != null) {
            throw new IllegalStateException( "Already created" );
        }
        
        this.ruby = runtimePool.borrowRuntime();
    }

    public synchronized void start() throws Exception {
        if (this.ruby == null) {
            throw new IllegalStateException( "Not created" );
        }
        ReflectionHelper.callIfPossible( this.ruby, getService(), "start", null );
        this.started = true;
    }

    public synchronized void stop() throws Exception {
        if (this.ruby == null) {
            throw new IllegalStateException( "Not created" );
        }
        ReflectionHelper.callIfPossible( this.ruby, getService(), "stop", null );
        this.started = false;
    }
    
    public synchronized void destroy() throws Exception {
        if (this.ruby == null) {
            throw new IllegalStateException( "Not created" );
        }
        runtimePool.returnRuntime( this.ruby );
        this.ruby = null;
    }
    
    public synchronized boolean isCreated() {
        return this.ruby != null;
    }
    
    public synchronized boolean isStarted() {
        return this.started;
    }
    
    public synchronized boolean isStopped() {
        return ! this.started;
    }
    
    public synchronized String getStatus() {
        if ( ! isCreated() ) {
            return "NOT CREATED";
        }
        
        if ( isStarted() ) {
            return "STARTED";
        }
        
        return "STOPPED";
    }

    protected IRubyObject getService() throws Exception {
        if (this.service == null) {
            this.service = resolver.resolve( this.ruby );
        }
        return this.service;
    }
    
    @Override
    public void setInjectableRegistry(InjectableRegistry injectableRegistry) {
        this.injectableRegistry = injectableRegistry;
    }
    
    public InjectableRegistry getInjectableRegistry() {
        return this.injectableRegistry;
    }
    
    private boolean started = false;

    private RubyRuntimePool runtimePool;
    private Ruby ruby;
    private IRubyObject service;
    private RubyComponentResolver resolver;
    
    private InjectableRegistry injectableRegistry;
    
}
