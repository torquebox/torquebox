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
    
    public static final ServiceName INJECTABLE_HANDLER_REGISTRY   = INJECTION.append( "injectable-handler-registry" );
    
    
    public static ServiceName runtimeFactoryName(final String deploymentName) {
        return RUNTIME.append("factory").append( deploymentName );
    }
    
    public static ServiceName runtimePoolName(DeploymentUnit unit, final String poolName) {
        return unit.getServiceName().append("runtime-pool").append( poolName );
    }
    
 
}
