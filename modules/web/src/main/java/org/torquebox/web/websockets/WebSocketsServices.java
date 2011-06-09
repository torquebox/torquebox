package org.torquebox.web.websockets;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.web.as.WebServices;

public class WebSocketsServices {

    public static final ServiceName WEB_SOCKETS = WebServices.WEB.append( "websockets" );
    public static final ServiceName WEB_SOCKETS_SERVER = WEB_SOCKETS.append( "server" );

    
    public static final ServiceName webSocketContext(DeploymentUnit unit, String contextPath) {
        return unit.getServiceName().append( WEB_SOCKETS ).append( contextPath );
    }
    
    public static final ServiceName webSocketProcessorComponentResolver(DeploymentUnit unit, String contextPath) {
        return webSocketContext( unit, contextPath ).append( "resolver" );
    }

}
