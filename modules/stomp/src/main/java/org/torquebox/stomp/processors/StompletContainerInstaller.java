/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.stomp.processors;

import java.util.List;

import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.web.WebApplicationMetaData;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.stomplet.server.StompletServer;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.StompApplicationMetaData;
import org.torquebox.stomp.StompEndpointBinding;
import org.torquebox.stomp.StompEndpointBindingService;
import org.torquebox.stomp.StompletContainerService;
import org.torquebox.stomp.as.StompServices;

public class StompletContainerInstaller implements DeploymentUnitProcessor {

    public StompletContainerInstaller(String socketBindingRef) {
        this.socketBindingRef = socketBindingRef;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        List<RubyStompletMetaData> allMetaData = unit.getAttachmentList( RubyStompletMetaData.ATTACHMENTS_KEY );

        if (allMetaData.isEmpty()) {
            return;
        }

        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );

        List<String> hosts = stompAppMetaData.getHosts();

        StompletContainerService containerService = new StompletContainerService();
        containerService.setHosts( hosts );

        ServiceName containerName = StompServices.container( unit );

        phaseContext.getServiceTarget().addService( containerName, containerService )
                .addDependency( StompServices.SERVER, StompletServer.class, containerService.getStompletServerInjector() )
                .addDependency( StompServices.container( unit ).append( "session-manager" ), StompSessionManager.class, containerService.getSessionManagerInjector() )
                .install();

        String host = null;

        if (!hosts.isEmpty()) {
            host = hosts.get( 0 );
        }

        String contextPath = null;

        WebApplicationMetaData webAppMetaData = unit.getAttachment( WebApplicationMetaData.ATTACHMENT_KEY );

        if (webAppMetaData != null) {
            contextPath = webAppMetaData.getContextPath();
        }

        if (contextPath == null) {
            contextPath = stompAppMetaData.getContextPath();
        } else {
            if (contextPath.endsWith( "/" )) {
                contextPath = contextPath.substring(0, contextPath.length() - 1 ) + stompAppMetaData.getContextPath();
            } else {
                contextPath = contextPath + stompAppMetaData.getContextPath();
            }
        }

        StompEndpointBindingService bindingService = new StompEndpointBindingService( host, contextPath );

        ServiceBuilder<String> builder = phaseContext.getServiceTarget().addService( StompServices.endpointBinding( unit ), bindingService )
                // .addDependency( containerName );
                .addDependency( SocketBinding.JBOSS_BINDING_NAME.append( socketBindingRef ), SocketBinding.class, bindingService.getSocketBindingInjector() );

        if (webAppMetaData != null) {
            if (host == null) {
                host = "default-host";
            }
            builder.addDependency( DependencyType.OPTIONAL,
                    WebSubsystemServices.JBOSS_WEB_HOST.append( host ),
                    VirtualHost.class,
                    bindingService.getVirtualHostInjector() );
        }

        builder.install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

    private String socketBindingRef;
}
