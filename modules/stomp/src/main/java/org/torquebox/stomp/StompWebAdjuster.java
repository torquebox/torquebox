package org.torquebox.stomp;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.web.rack.RackApplicationMetaData;

public class StompWebAdjuster implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        
        StompApplicationMetaData stompAppMetaData = unit.getAttachment( StompApplicationMetaData.ATTACHMENT_KEY );
        System.err.println( "ADJUSTER BEFORE stompAppMetaData=" + stompAppMetaData );
        
        if ( stompAppMetaData == null ) {
            return;
        }
        
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );
        System.err.println( "rackAppMetaData=" + rackAppMetaData );
        
        if ( rackAppMetaData == null ) {
            return;
        }
        
        if ( stompAppMetaData.getContextPath() == null ) {
            stompAppMetaData.setContextPath( rackAppMetaData.getContextPath() );
        }
        
        if ( stompAppMetaData.getHosts().isEmpty() ) {
            System.err.println( "STOMP no hosts, set to " + rackAppMetaData.getHosts() );
            stompAppMetaData.setHosts( rackAppMetaData.getHosts() );
        }
        
        System.err.println( "ADJUSTER AFTER stompAppMetaData=" + stompAppMetaData );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
