package org.torquebox.stomp;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.projectodd.stilts.stomplet.StompletServer;
import org.torquebox.stomp.as.StompServices;

public class StompletContainerDeployer implements DeploymentUnitProcessor {

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
            .install();
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
