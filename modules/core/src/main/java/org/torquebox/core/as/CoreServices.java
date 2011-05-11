package org.torquebox.core.as;

import org.jboss.msc.service.ServiceName;

public class CoreServices {

    private CoreServices() {
    }

    public static final ServiceName TORQUEBOX = ServiceName.of( "torquebox" );
    public static final ServiceName CORE      = TORQUEBOX.append( "core" );
    public static final ServiceName RUNTIME   = CORE.append( "runtime" );
    
    public static ServiceName runtimeFactoryName(final String deploymentName) {
        return RUNTIME.append("factory").append( deploymentName );
    }
    
    public static ServiceName runtimePoolName(final String deploymentName, final String poolName) {
        return RUNTIME.append("pool").append( deploymentName ).append( poolName );
    }

}
