package org.torquebox.stomp;

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
import org.torquebox.stomp.as.StompServices;
import org.torquebox.web.rack.RackApplicationMetaData;

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

        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        boolean useWeb = false;
        
        String hostName = null;
        String context = null;

        if (rackAppMetaData != null) {
            List<String> webHosts = rackAppMetaData.getHosts();
            List<String> stompHosts = stompAppMetaData.getHosts();

            if (stompHosts.isEmpty()) {
                useWeb = true;
            } else if (webHosts.isEmpty()) {
                useWeb = false;
            } else if (stompHosts.get( 0 ).equals( webHosts.get( 0 ) )) {
                useWeb = true;
            }
            
            if ( useWeb ) {
                hostName = webHosts.get(  0 );
                context = rackAppMetaData.getContextPath();
            } else {
                hostName = stompHosts.get( 0 );
                context = stompAppMetaData.getContextPath();
            }
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

    }

    private String defaultHost;

}
