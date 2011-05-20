package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.injection.analysis.Injectable;
import org.torquebox.core.injection.analysis.InjectableHandlerRegistry;

public class PredeterminedInjectableProcessor implements DeploymentUnitProcessor {

    public PredeterminedInjectableProcessor(InjectableHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY )) {
            for (Injectable each : this.registry.getPredeterminedInjectables()) {
                System.err.println( "Adding predetermined injectable: " + each );
                unit.addToAttachmentList( ComponentResolver.ADDITIONAL_INJECTABLES, each );
            }
        }

    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    private InjectableHandlerRegistry registry;

}
