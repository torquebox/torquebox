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

package org.torquebox.messaging.as;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;

public class MessagingServices {

    private MessagingServices() {
    }

    public static final ServiceName MESSAGING = CoreServices.TORQUEBOX.append( "messaging" );
    public static final ServiceName RUBY_CONNECTION_FACTORY = MESSAGING.append(  "ruby-connection-factory" );
    public static final ServiceName RUBY_XA_CONNECTION_FACTORY = MESSAGING.append(  "ruby-xa-connection-factory" );
    public static final ServiceName DESTINATIONIZER = MESSAGING.append( "destinationizer" );

    public static final ServiceName WEBSOCKETS = MESSAGING.append( "websockets" );
    public static final ServiceName WEBSOCKETS_SERVER = WEBSOCKETS.append( "server" );

    public static ServiceName messageProcessor(DeploymentUnit unit, String processorName) {
        return unit.getServiceName().append( MESSAGING ).append( processorName );
    }

    public static ServiceName messageProcessorComponentResolver(DeploymentUnit unit, String processorName) {
        return unit.getServiceName().append( MESSAGING ).append(  processorName ).append( "resolver" );
    }

    public static ServiceName webSocketProcessor(DeploymentUnit unit) {
        return unit.getServiceName().append( WEBSOCKETS ).append( "processor"  );
    }

    public static ServiceName destinationizer(DeploymentUnit unit) {
        return unit.getServiceName().append( DESTINATIONIZER );
    }

    public static ServiceName webSocketProcessorComponentResolver(DeploymentUnit unit) {
        return webSocketProcessor( unit ).append( "resolver" );
    }
    
    
}
