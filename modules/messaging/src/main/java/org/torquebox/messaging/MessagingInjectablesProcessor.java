package org.torquebox.messaging;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.messaging.injection.ConnectionFactoryInjectable;

public class MessagingInjectablesProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if ( ! unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        unit.addToAttachmentList( ComponentResolver.ADDITIONAL_INJECTABLES, ConnectionFactoryInjectable.INSTANCE );
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
