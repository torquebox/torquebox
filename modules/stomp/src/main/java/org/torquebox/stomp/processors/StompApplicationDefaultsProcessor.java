package org.torquebox.stomp.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.stomp.StompApplicationMetaData;

public class StompApplicationDefaultsProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );
        
        if ( stompAppMetaData == null ) {
            return;
        }
        
        if ( stompAppMetaData.getContextPath() == null ) {
            stompAppMetaData.setContextPath( "/" );
        }
        
        if ( stompAppMetaData.getHosts().isEmpty() ) {
            stompAppMetaData.addHost( "localhost" );
        }
        
        System.err.println( "STOMP HOSTS: " + stompAppMetaData.getHosts() );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
