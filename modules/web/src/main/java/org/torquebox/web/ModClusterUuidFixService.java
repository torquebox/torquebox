/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web;

import org.apache.catalina.Engine;
import org.jboss.as.web.WebServer;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * This class exists solely to work around JBPAPP-8451 and TORQUE-755.
 * Once we've upgraded to AS 7.1.2, this entire class can go away.
 * 
 * @author bbrowning
 *
 */
public class ModClusterUuidFixService implements Service<ModClusterUuidFixService> {

    public ModClusterUuidFixService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public void start(StartContext context) throws StartException {
        WebServer webServer = injectedWebServer.getValue();
        Engine engine = (Engine) webServer.getService().getContainer();
        if ("undefined".equals( engine.getJvmRoute() )) {
            engine.setJvmRoute( null );
        }
    }

    public void stop(StopContext context) {
        // nothing to do
    }

    public Injector<WebServer> getWebServerInjector() {
        return injectedWebServer;
    }

    private final InjectedValue<WebServer> injectedWebServer = new InjectedValue<WebServer>();

}
