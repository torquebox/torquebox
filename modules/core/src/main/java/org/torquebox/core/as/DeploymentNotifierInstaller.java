package org.torquebox.core.as;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.core.app.RubyApplicationMetaData;

public class DeploymentNotifierInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if ( ! unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        List<ServiceName> serviceNames = unit.getAttachmentList( DeploymentNotifier.SERVICES_ATTACHMENT_KEY );
        
        phaseContext.getServiceTarget().addService( unit.getServiceName().append( "notifier" ), new DeploymentNotifier( unit ) )
            .addDependencies( serviceNames )
            .setInitialMode( Mode.PASSIVE )
            .install();
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub
        
    }

}
