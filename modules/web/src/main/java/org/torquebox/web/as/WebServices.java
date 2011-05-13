package org.torquebox.web.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;

public class WebServices {

    private WebServices() {
    }

    public static final ServiceName TORQUEBOX = ServiceName.of( "torquebox" );
    public static final ServiceName WEB       = TORQUEBOX.append( "web" );
    public static final ServiceName RACK      = WEB.append( "rack" );
    
    public static ServiceName rackApplicationFactoryName(final String name) {
        return RACK.append("factory").append( name );
    }
    public static ServiceName rackApplicationPoolName(final String name) {
        return RACK.append("pool").append( name );
    }
    
    public static ServiceName rackApplicationComponentResolver() {
        return ServiceName.of(  "rack"  ).append(  "application"  );
    }
    
    public static ServiceName rackApplicationComponentResolver(DeploymentUnit unit) {
        return unit.getServiceName().append( rackApplicationComponentResolver() );
    }
    

}
