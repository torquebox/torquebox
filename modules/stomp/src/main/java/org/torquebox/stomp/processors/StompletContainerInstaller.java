package org.torquebox.stomp.processors;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.stomplet.server.StompletServer;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.StompApplicationMetaData;
import org.torquebox.stomp.StompletContainerService;
import org.torquebox.stomp.as.StompServices;

public class StompletContainerInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        List<RubyStompletMetaData> allMetaData = unit.getAttachmentList( RubyStompletMetaData.ATTACHMENTS_KEY );
        
        if ( allMetaData.isEmpty() ) {
            return;
        }
        
        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );
        
        StompletContainerService service = new StompletContainerService();
        service.setHosts( stompAppMetaData.getHosts() );
        
        phaseContext.getServiceTarget().addService( StompServices.container( unit ), service )
            .addDependency( StompServices.SERVER, StompletServer.class, service.getStompletServerInjector() )
            .addDependency( StompServices.container( unit ).append(  "session-manager" ), StompSessionManager.class, service.getSessionManagerInjector() )
            .install();
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
