package org.torquebox.security.auth.as;

import org.jboss.as.security.ModuleName;
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

public class AuthDependencyProcessor implements DeploymentUnitProcessor {

    public static final ModuleIdentifier PICKETBOX_ID = ModuleIdentifier
            .create( ModuleName.PICKETBOX.getName(),
                    ModuleName.PICKETBOX.getSlot() );

    private static ModuleIdentifier TORQUEBOX_AUTH_ID = ModuleIdentifier.create("org.torquebox.auth");
    
    @Override
    /** {@inheritDoc} */
    public void deploy(DeploymentPhaseContext phaseContext)
            throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        final ModuleSpecification moduleSpecification = deploymentUnit
                .getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpecification.addDependency( new ModuleDependency( moduleLoader,
                TORQUEBOX_AUTH_ID, false, true, false ) );
    }

    @Override
    /** {@inheritDoc} */
    public void undeploy(DeploymentUnit unit) {
    }

}
