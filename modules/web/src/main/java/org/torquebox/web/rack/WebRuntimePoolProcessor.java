package org.torquebox.web.rack;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.runtime.PoolMetaData;

public class WebRuntimePoolProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if ( ! unit.hasAttachment( RackApplicationMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        List<PoolMetaData> allMetaData = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        
        PoolMetaData poolMetaData = null;
        
        for ( PoolMetaData each : allMetaData ) {
            if ( each.getName().equals( "web" ) ) {
                poolMetaData = each;
                break;
            }
        }
        
        if ( poolMetaData == null ) {
            poolMetaData = new PoolMetaData("web");
            poolMetaData.setShared();
            unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, poolMetaData );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
