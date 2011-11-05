package org.torquebox.core.processors;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.component.processors.DeploymentUtils;

public class RootedDeploymentProcessor implements DeploymentUnitProcessor {

    private DeploymentUnitProcessor delegate;

    private RootedDeploymentProcessor(DeploymentUnitProcessor delegate) {
        this.delegate = delegate;
    }
    
    public static DeploymentUnitProcessor rootSafe(DeploymentUnitProcessor processor) {
        return new RootedDeploymentProcessor(processor);
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRooted( phaseContext.getDeploymentUnit() )) {
            delegate.deploy( phaseContext );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        delegate.undeploy( context );
    }

}
