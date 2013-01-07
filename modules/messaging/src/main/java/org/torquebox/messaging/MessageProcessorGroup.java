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

package org.torquebox.messaging;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.polyglot.messaging.BaseMessageProcessorGroup;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;

public class MessageProcessorGroup extends BaseMessageProcessorGroup implements MessageProcessorGroupMBean {

    public MessageProcessorGroup(ServiceRegistry registry, ServiceName baseServiceName, String destinationName) {
        super( registry, baseServiceName, destinationName, MessageProcessor.class );
    }

    public Injector<RubyRuntimePool> getRuntimePoolInjector() {
        return this.runtimePoolInjector;
    }

    public Injector<ComponentResolver> getComponentResolverInjector() {
        return this.componentResolverInjector;
    }

    public synchronized RubyRuntimePool getRubyRuntimePool() {
        if (this.runtimePool == null) {
            this.runtimePool = this.runtimePoolInjector.getValue();
        }
        return this.runtimePool;
    }

    public ComponentResolver getComponentResolver() {
        return this.componentResolverInjector.getValue();
    }

    private RubyRuntimePool runtimePool;

    private final InjectedValue<RubyRuntimePool> runtimePoolInjector = new InjectedValue<RubyRuntimePool>();
    private final InjectedValue<ComponentResolver> componentResolverInjector = new InjectedValue<ComponentResolver>();

}
