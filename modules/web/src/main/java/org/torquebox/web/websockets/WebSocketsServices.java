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

package org.torquebox.web.websockets;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.web.as.WebServices;

/**
 * Convenience methods for creating <code>ServiceName</code>s related to
 * web-sockets.
 * 
 * @author Bob McWhirter
 */
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
