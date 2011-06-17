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

import java.util.List;

import org.apache.catalina.Context;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RubyRuntimePool;
import org.torquebox.web.rack.RackApplicationMetaData;

/** Installs live <code>WebSocketContext</code> services.
 * 
 * @author Bob McWhirter
 */
public class WebSocketContextInstaller implements DeploymentUnitProcessor {

    /** Construct.
     * 
     * @param defaultHost The default host for applications specifying no host.
     */
    public WebSocketContextInstaller(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        List<WebSocketMetaData> allMetaData = unit.getAttachmentList( WebSocketMetaData.ATTACHMENTS_KEY );

        for (WebSocketMetaData each : allMetaData) {
            deploy( phaseContext, each );
        }
    }

    protected void deploy(DeploymentPhaseContext phaseContext, WebSocketMetaData webSocketMetaData) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        String appContext = rackAppMetaData.getContextPath();
        String socketContext = webSocketMetaData.getContextPath();
        
        String contextPath = null;

        if (appContext == null || appContext.trim().equals( "" )) {
            contextPath = socketContext;
        } else {
            String cleanSocketContext = socketContext;
            if ( cleanSocketContext.startsWith(  "/"  ) ) {
                cleanSocketContext = cleanSocketContext.substring(1);
            }
            if ( appContext.endsWith("/" ) ) {
                contextPath = appContext + cleanSocketContext;
            } else {
                contextPath = appContext + "/" + cleanSocketContext;
            }
        }
        
        if ( ! contextPath.endsWith( "/" ) ) {
            contextPath = contextPath + "/";
        }
        
        URLRegistry urlRegistry = unit.getAttachment( URLRegistry.ATTACHMENT_KEY );
        
        if ( urlRegistry == null ) {
            urlRegistry = new URLRegistry();
            unit.putAttachment( URLRegistry.ATTACHMENT_KEY, urlRegistry );
        }

        String contextName = webSocketMetaData.getName();
        ServiceName serviceName = WebSocketsServices.webSocketContext( unit, contextName );
        WebSocketContextService service = new WebSocketContextService( urlRegistry, contextName, contextPath );

        phaseContext
                .getServiceTarget()
                .addService( serviceName, service )
                .addDependency( WebSocketsServices.WEB_SOCKETS_SERVER, WebSocketsServer.class, service.getServerInjector() )
                .addDependency( WebSubsystemServices.JBOSS_WEB.append(unit.getName()), Context.class, service.getContextInjector() )
                .addDependency( getHostServiceName( unit ), VirtualHost.class, service.getHostInjector() )
                .addDependency( WebSocketsServices.webSocketProcessorComponentResolver( unit, contextName ), ComponentResolver.class, service.getComponentResolverInjector() )
                .addDependency( CoreServices.runtimePoolName( unit, "websockets" ).append(  "START" ), RubyRuntimePool.class, service.getRuntimePoolInjector() )
                .setInitialMode( Mode.ACTIVE )
                .install();
    }

    protected ServiceName getHostServiceName(DeploymentUnit unit) {
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        List<String> hosts = rackAppMetaData.getHosts();

        String hostName = null;

        if (hosts == null || hosts.isEmpty()) {
            hostName = this.defaultHost;
        } else {
            hostName = hosts.get( 0 );
        }

        return WebSubsystemServices.JBOSS_WEB_HOST.append( hostName );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

    private String defaultHost;

}
