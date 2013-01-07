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

package org.torquebox.services.injection;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.services.as.ServicesServices;

/** Injectable for Ruby services.
 * 
 * @see ServiceInjectableHandler
 * 
 * @author Bob McWhirter
 */
public class ServiceInjectable extends SimpleNamedInjectable {
    
    public static final String TYPE = "service";

    public ServiceInjectable(String name) {
        super( TYPE, name, false );
    }
    
    public String getKey() {
        return TYPE + ":" + getName();
    }

    @Override
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) throws Exception {
        ServiceName serviceName = ServicesServices.serviceInjectableService( unit, getName() );
        return serviceName;
    }
    
}
