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

package org.torquebox.core.component;

import org.jboss.as.naming.context.NamespaceContextSelector;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class ComponentResolverService implements Service<ComponentResolver> {

    public ComponentResolverService(ComponentResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    public ComponentResolver getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resolver;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resolver.setNamespaceContextSelector( this.selectorInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        
    }

    public Injector<NamespaceContextSelector> getNamespaceContextSelectorInjector() {
        return this.selectorInjector;
    }
    
    private InjectedValue<NamespaceContextSelector> selectorInjector = new InjectedValue<NamespaceContextSelector>();
    
    private ComponentResolver resolver;
}
