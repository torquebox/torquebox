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

package org.torquebox.messaging.injection;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class RubyXaConnectionFactoryService implements Service<RubyXaConnectionFactory> {

    @Override
    public RubyXaConnectionFactory getValue() throws IllegalStateException, IllegalArgumentException {
        return this.rubyConnectionFactory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.rubyConnectionFactory = new RubyXaConnectionFactory( this.connectionFactoryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.rubyConnectionFactory = null;
    }
    
    public Injector<HornetQConnectionFactory> getConnectionFactoryInjector() {
        return this.connectionFactoryInjector;
    }
    
    private RubyXaConnectionFactory rubyConnectionFactory;
    private InjectedValue<HornetQConnectionFactory> connectionFactoryInjector = new InjectedValue<HornetQConnectionFactory>();

}
