package org.torquebox.security.as;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

public class SecurityDependencyProcessor implements DeploymentUnitProcessor {

    private static ModuleIdentifier TORQUEBOX_SECURITY_ID = ModuleIdentifier.create("org.torquebox.security");
    
    @Override
    /** {@inheritDoc} */
    public void deploy(DeploymentPhaseContext phaseContext)
            throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpecification.addDependency( new ModuleDependency( moduleLoader,
                TORQUEBOX_SECURITY_ID, false, true, false ) );
    }

    @Override
    /** {@inheritDoc} */
    public void undeploy(DeploymentUnit unit) {
    }

}
