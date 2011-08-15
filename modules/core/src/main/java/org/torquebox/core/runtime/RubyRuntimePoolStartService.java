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

package org.torquebox.core.runtime;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class RubyRuntimePoolStartService implements Service<RubyRuntimePool> {

    public RubyRuntimePoolStartService(RubyRuntimePool pool) {
        this.pool = pool;
    }

    @Override
    public RubyRuntimePool getValue() throws IllegalStateException, IllegalArgumentException {
        return pool;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        context.execute( new Runnable() {
            @Override
            public void run() {
                try {
                    RubyRuntimePoolStartService.this.pool.start();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        } );
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.pool.stop();
        } catch (Exception e) {
            // swallow
        }
    }

    public Injector<RubyRuntimeFactory> getRubyRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }

    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();
    private RubyRuntimePool pool;

}
