package org.torquebox.web.websockets;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.web.as.WebServices;

public class WebSocketsServices {

    public static final ServiceName WEB_SOCKETS = WebServices.WEB.append( "websockets" );
    public static final ServiceName WEB_SOCKETS_SERVER = WEB_SOCKETS.append( "server" );

    public static final ServiceName urlRegistry(DeploymentUnit unit) {
        return unit.getServiceName().append( WEB_SOCKETS ).append( "url-registry" );
    }
    
    public static final ServiceName webSocketContext(DeploymentUnit unit, String name) {
        return unit.getServiceName().append( WEB_SOCKETS ).append( name );
    }
    
    public static final ServiceName webSocketProcessorComponentResolver(DeploymentUnit unit, String name) {
        return webSocketContext( unit, name ).append( "resolver" );
    }
    
    public static final WebSocketsServices INSTANCE = new WebSocketsServices();
    
    public ServiceName getUrlRegistryName(DeploymentUnit unit) {
        return urlRegistry( unit );
    }

}
