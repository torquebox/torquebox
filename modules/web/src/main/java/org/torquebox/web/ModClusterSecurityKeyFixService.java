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

import java.lang.reflect.Field;

import org.jboss.as.modcluster.ModCluster;
import org.jboss.modcluster.ModClusterService;
import org.jboss.modcluster.advertise.AdvertiseListener;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class ModClusterSecurityKeyFixService implements Service<ModClusterSecurityKeyFixService> {

    @Override
    public ModClusterSecurityKeyFixService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ModCluster modCluster = injectedModCluster.getOptionalValue();
        if (modCluster != null) {
            try {
                Field serviceField = modCluster.getClass().getDeclaredField( "service" );
                serviceField.setAccessible( true );
                ModClusterService service = (ModClusterService) serviceField.get( modCluster );
                Field advertiseListenerField = service.getClass().getDeclaredField( "advertiseListener" );
                advertiseListenerField.setAccessible( true );
                AdvertiseListener listener = (AdvertiseListener) advertiseListenerField.get( service );
                if (listener != null) {
                    Field securityKeyField = listener.getClass().getDeclaredField( "securityKey" );
                    securityKeyField.setAccessible( true );
                    String securityKey = (String) securityKeyField.get( listener );
                    if ("undefined".equals( securityKey )) {
                        Field mdField = listener.getClass().getDeclaredField( "md" );
                        mdField.setAccessible( true );
                        mdField.set( listener, null );
                    }
                }
            } catch (Exception e) {
                throw new StartException( e );
            }
        }
    }

    @Override
    public void stop(StopContext context) {
        // nothing to do
    }

    public Injector<ModCluster> getModClusterInjector() {
        return injectedModCluster;
    }

    private final InjectedValue<ModCluster> injectedModCluster = new InjectedValue<ModCluster>();

}
