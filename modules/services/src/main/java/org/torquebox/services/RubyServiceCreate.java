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

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.polyglot.core.AsyncService;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RestartableRubyRuntimePool;

public class RubyServiceCreate extends AsyncService<RubyService> {
    
    public RubyServiceCreate(RubyService service) {
        this.service = service;
    }

    @Override
    public RubyService getValue() throws IllegalStateException, IllegalArgumentException {
        return this.service;
    }

    @Override
    public void startAsync(final StartContext context) throws Exception {
        this.service.setComponentResolver( this.componentResolverInjector.getValue() );
        this.service.setRubyRuntimePool( this.rubyRuntimePoolInjector.getValue() );
        this.service.create();
    }

    @Override
    public void stop(StopContext context) {
        this.service.destroy();
    }
    
    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }
    
    public Injector<RestartableRubyRuntimePool> getRubyRuntimePoolInjector() {
        return this.rubyRuntimePoolInjector;
    }
    
    private RubyService service;
    private InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();
    private InjectedValue<RestartableRubyRuntimePool> rubyRuntimePoolInjector = new InjectedValue<RestartableRubyRuntimePool>();

}
