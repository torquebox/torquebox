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

package org.torquebox.test.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.ServiceRegistry;

public class MockServiceRegistry implements ServiceRegistry {

    @Override
    public ServiceController<?> getRequiredService(ServiceName serviceName) throws ServiceNotFoundException {
        ServiceController<?> service = registry.get( serviceName );
        if ( service == null ) {
            throw new ServiceNotFoundException();
        }
        
        return service;
    }

    @Override
    public ServiceController<?> getService(ServiceName serviceName) {
        return registry.get(  serviceName  );
    }

    @Override
    public List<ServiceName> getServiceNames() {
        List<ServiceName> names = new ArrayList<ServiceName>();
        names.addAll( registry.keySet() );
        return names;
    }
    
    private Map<ServiceName,ServiceController<?>> registry = new HashMap<ServiceName,ServiceController<?>>();

}
