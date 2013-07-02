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

package org.torquebox.core.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class CoreServices {

    private CoreServices() {
    }

    public static final ServiceName TORQUEBOX = ServiceName.of( "torquebox" );
    public static final ServiceName CORE      = TORQUEBOX.append( "core" );
    public static final ServiceName RUNTIME   = CORE.append( "runtime" );
    public static final ServiceName INJECTION = CORE.append( "injection" );
    
    public static final ServiceName GLOBAL_RUBY                   = RUNTIME.append( "global" );
    public static final ServiceName INJECTABLE_HANDLER_REGISTRY   = INJECTION.append( "injectable-handler-registry" );
    public static final ServiceName RUNTIME_INJECTION_ANALYZER    = INJECTION.append( "runtime-injection-analyzer" );
    public static final ServiceName RUNTIME_RESTART_SCANNER      = CORE.append( "runtime-restart-scanner" );
    
    
    public static ServiceName runtimeFactoryName(final DeploymentUnit unit) {
        return unit.getServiceName().append( RUNTIME ).append("factory");
    }
    
    public static ServiceName runtimePoolName(DeploymentUnit unit, final String poolName) {
        return unit.getServiceName().append( RUNTIME).append("pool").append( poolName );
    }

    public static ServiceName runtimeStartPoolName(DeploymentUnit unit, final String poolName) {
        return runtimePoolName( unit, poolName).append( "START" );
    }

    public static ServiceName serviceRegistryName(DeploymentUnit unit) {
        return unit.getServiceName().append( CORE ).append( "service-registry" );
    }
    
    public static ServiceName serviceTargetName(DeploymentUnit unit) {
        return unit.getServiceName().append( CORE ).append( "service-target" );
    }
    
    public static ServiceName appNamespaceContextSelector(DeploymentUnit unit) {
        return unit.getServiceName().append( CORE ).append( "ns-context-selector" );
    }
    
 
}
