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

import org.apache.catalina.Context;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.conduit.stomp.SimpleStompSessionManager;
import org.torquebox.stomp.HttpStompSessionManagerService;
import org.torquebox.stomp.StompApplicationMetaData;
import org.torquebox.stomp.as.StompServices;
import org.torquebox.web.rack.RackMetaData;

public class SessionManagerInstaller implements DeploymentUnitProcessor {

    public SessionManagerInstaller(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );

        if (stompAppMetaData == null) {
            return;
        }

        RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );

        boolean useWeb = false;

        String hostName = null;
        String context = null;

        if (rackAppMetaData != null) {
            List<String> webHosts = rackAppMetaData.getHosts();
            List<String> stompHosts = stompAppMetaData.getHosts();

            System.err.println( "webhosts: " + webHosts );
            System.err.println( "stomphosts: " + stompHosts );

            if (stompHosts.isEmpty()) {
                useWeb = true;
            } else if (webHosts.isEmpty()) {
                useWeb = false;
            } else if (stompHosts.get( 0 ).equals( webHosts.get( 0 ) )) {
                useWeb = true;
            }

            if (useWeb) {
                if (!webHosts.isEmpty()) {
                    hostName = webHosts.get( 0 );
                }
                context = rackAppMetaData.getContextPath();
            } else {
                if (!stompHosts.isEmpty()) {
                    hostName = stompHosts.get( 0 );
                }
                context = stompAppMetaData.getContextPath();
            }
        }

        if ( hostName == null ) {
            hostName = "default-host";
        }
        
        if (useWeb) {
            deployWebBasedSessionManager( phaseContext, hostName, context );
        } else {
            deployStandaloneSessionManager( phaseContext );
        }
    }

    protected void deployWebBasedSessionManager(DeploymentPhaseContext phaseContext, String hostName, String context) {
        System.err.println( "DEPLOY WEB MANAGER" );
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        HttpStompSessionManagerService service = new HttpStompSessionManagerService();
        ServiceName serviceName = StompServices.container( unit ).append( "session-manager" );

        ServiceName contextServiceName = WebSubsystemServices.deploymentServiceName( hostName, context );
        phaseContext.getServiceTarget().addService( serviceName, service )
                .addDependency( contextServiceName, Context.class, service.getContextInjector() )
                .install();
    }

    protected void deployStandaloneSessionManager(DeploymentPhaseContext phaseContext) {
        System.err.println( "DEPLOY STANDALONE SESSION MANAGER" );
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        SimpleStompSessionManager sessionManager = new SimpleStompSessionManager();
        ServiceName serviceName = StompServices.container( unit ).append( "session-manager" );
        ValueService<StompSessionManager> service = new ValueService<StompSessionManager>( new ImmediateValue<StompSessionManager>( sessionManager ) );
        phaseContext.getServiceTarget().addService( serviceName, service )
                .install();
    }

    protected ServiceName getWebHostServiceName(DeploymentUnit unit) {
        RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );
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

    }

    private String defaultHost;

}
