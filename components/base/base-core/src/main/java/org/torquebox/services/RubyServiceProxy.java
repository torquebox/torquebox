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
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyServiceProxy {

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

    public void start() throws Exception {
        if (this.ruby != null)
            throw new IllegalStateException( "Already running" );
        this.ruby = runtimePool.borrowRuntime();
        ReflectionHelper.callIfPossible( this.ruby, getService(), "start", null );
    }

    public void stop() throws Exception {
        if (this.ruby == null)
            throw new IllegalStateException( "Not running" );
        ReflectionHelper.callIfPossible( this.ruby, getService(), "stop", null );
        runtimePool.returnRuntime( this.ruby );
        this.ruby = null;
    }

    protected IRubyObject getService() throws Exception {
        if (this.service == null) {
            this.service = resolver.resolve( this.ruby );
        }
        return this.service;
    }

    private RubyRuntimePool runtimePool;
    private Ruby ruby;
    private IRubyObject service;
    private RubyComponentResolver resolver;
}
