package org.torquebox.web.websockets;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.value.ImmediateValue;

public class URLRegistryInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        URLRegistry urlRegistry = unit.getAttachment( URLRegistry.ATTACHMENT_KEY );
        
        if ( urlRegistry == null ) {
            return;
        }
        
        ServiceName serviceName = WebSocketsServices.urlRegistry( unit );
        Service<URLRegistry> service = new ValueService<URLRegistry>( new ImmediateValue<URLRegistry>( urlRegistry ) );
        phaseContext.getServiceTarget().addService( serviceName, service )
            .setInitialMode( Mode.ACTIVE )
            .install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
