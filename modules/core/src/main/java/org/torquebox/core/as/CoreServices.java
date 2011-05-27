package org.torquebox.core.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class CoreServices {

    private CoreServices() {
    }

    public static final ServiceName TORQUEBOX = ServiceName.of( "torquebox" );
    public static final ServiceName CORE      = TORQUEBOX.append( "core" );
    public static final ServiceName RUNTIME   = CORE.append( "runtime" );
    public static final ServiceName INJECTION   = CORE.append( "injection" );
    
    public static final ServiceName GLOBAL_RUBY                   = RUNTIME.append( "global" );
    public static final ServiceName INJECTABLE_HANDLER_REGISTRY   = INJECTION.append( "injectable-handler-registry" );
    
    
    public static ServiceName runtimeFactoryName(final DeploymentUnit unit) {
        return unit.getServiceName().append( RUNTIME ).append("factory");
    }
    
    public static ServiceName runtimePoolName(DeploymentUnit unit, final String poolName) {
        return unit.getServiceName().append( RUNTIME).append("pool").append( poolName );
    }

    public static ServiceName serviceRegistryName(DeploymentUnit unit) {
        return unit.getServiceName().append( CORE ).append( "service-registry" );
    }
    
    public static ServiceName serviceTargetName(DeploymentUnit unit) {
        return unit.getServiceName().append( CORE ).append( "service-target" );
    }
    
 
}
