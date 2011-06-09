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

public class WebSocketContextInstaller implements DeploymentUnitProcessor {

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

        ServiceName serviceName = WebSocketsServices.webSocketContext( unit, socketContext );
        WebSocketContextService service = new WebSocketContextService( contextPath );

        phaseContext
                .getServiceTarget()
                .addService( serviceName, service )
                .addDependency( WebSocketsServices.WEB_SOCKETS_SERVER, WebSocketsServer.class, service.getServerInjector() )
                .addDependency( WebSubsystemServices.JBOSS_WEB.append(unit.getName()), Context.class, service.getContextInjector() )
                .addDependency( getHostServiceName( unit ), VirtualHost.class, service.getHostInjector() )
                .addDependency( WebSocketsServices.webSocketProcessorComponentResolver( unit, socketContext ), ComponentResolver.class, service.getComponentResolverInjector() )
                .addDependency( CoreServices.runtimePoolName( unit, "websockets" ), RubyRuntimePool.class, service.getRuntimePoolInjector() )
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
