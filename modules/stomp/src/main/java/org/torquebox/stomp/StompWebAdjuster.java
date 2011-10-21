package org.torquebox.stomp;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.web.rack.RackMetaData;

public class StompWebAdjuster implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        
        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );
        
        if ( stompAppMetaData == null ) {
            return;
        }
        
        RackMetaData rackAppMetaData = unit.getAttachment( RackMetaData.ATTACHMENT_KEY );
        
        if ( rackAppMetaData == null ) {
            return;
        }
        
        if ( stompAppMetaData.getContextPath() == null ) {
            stompAppMetaData.setContextPath( rackAppMetaData.getContextPath() );
        }
        
        if ( stompAppMetaData.getHosts().isEmpty() ) {
            stompAppMetaData.setHosts( rackAppMetaData.getHosts() );
        }
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
