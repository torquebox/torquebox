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

package org.torquebox.core.runtime;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class RubyRuntimeFactoryPoolService implements Service<RubyRuntimePool>{

    public RubyRuntimeFactoryPoolService(RubyRuntimePool pool) {
        this.pool = pool;
    }
    
    @Override
    public RubyRuntimePool getValue() throws IllegalStateException, IllegalArgumentException {
        return pool;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.pool.setInstanceFactory( this.runtimeFactoryInjector.getValue() );
        this.pool.setNamespaceContextSelector( this.selectorInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.pool.stop();
        } catch (Exception e) {
            log.warn(  "Error while stopping pool", e );
        }
        this.pool.setInstanceFactory( null );
    }
    
    public Injector<RubyRuntimeFactory> getRubyRuntimeFactoryInjector() {
        return this.runtimeFactoryInjector;
    }
    
    public Injector<NamespaceContextSelector> getNamespaceContextSelectorInjector() {
        return this.selectorInjector;
    }
    
    private static final Logger log = Logger.getLogger( "org.torquebox.runtime.pool" );
    
    private InjectedValue<RubyRuntimeFactory> runtimeFactoryInjector = new InjectedValue<RubyRuntimeFactory>();
    private InjectedValue<NamespaceContextSelector> selectorInjector = new InjectedValue<NamespaceContextSelector>();
    private RubyRuntimePool pool;


}
