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

package org.torquebox.services.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class ServicesServices {
    
    private ServicesServices() {
    }
    
    public static final ServiceName TORQUEBOX = ServiceName.of( "torquebox" );
    public static final ServiceName SERVICES = TORQUEBOX.append( "services" );
    
    public static ServiceName serviceComponentResolver(DeploymentUnit unit, String serviceName ) {
        return unit.getServiceName().append( "component_resolver" ).append( serviceName );
    }
    
    public static ServiceName serviceCreateRubyService(DeploymentUnit unit, String serviceName ) {
        return unit.getServiceName().append( "service" ).append( serviceName ).append( "create" );
    }
    
    public static ServiceName serviceStartRubyService(DeploymentUnit unit, String serviceName ) {
        return unit.getServiceName().append( "service" ).append( serviceName );
    }
    
    public static ServiceName serviceInjectableService(DeploymentUnit unit, String serviceName ) {
        return serviceStartRubyService( unit, serviceName ).append(  "injectable" );
    }

}
