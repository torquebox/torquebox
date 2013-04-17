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

package org.torquebox.web.as;

import org.jboss.as.web.WebSubsystemServices;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.polyglot.core.AsyncService;

/**
 * The entire purpose of this service is to ensure the HTTP web connector
 * is started. It was created solely in support of torquebox-lite on
 * Heroku where the web connector starts out disabled and then gets started
 * by this service after the Rack application has booted.
 * 
 * @author bbrowning
 *
 */
public class HttpConnectorStartService extends AsyncService<HttpConnectorStartService> {

    @Override
    public HttpConnectorStartService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void startAsync(final StartContext context) throws Exception {
        try {
            Thread.sleep( 200 );
        } catch (InterruptedException ignored) {}
        ServiceName serviceName = WebSubsystemServices.JBOSS_WEB_CONNECTOR.append("http");
        injectedServiceRegistry.getValue().getRequiredService(serviceName).setMode(Mode.ACTIVE);
    }

    @Override
    public void stop(StopContext context) {
        // Do nothing
    }

    public Injector<ServiceRegistry> getServiceRegistryInjector() {
        return injectedServiceRegistry;
    }

    private final InjectedValue<ServiceRegistry> injectedServiceRegistry = new InjectedValue<ServiceRegistry>();

}
