package org.torquebox.web.as;

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
import org.torquebox.web.rack.RackApplicationMetaData;

public class WebDependenciesProcessor implements DeploymentUnitProcessor {
    
    private static ModuleIdentifier TORQUEBOX_WEB_ID = ModuleIdentifier.create("org.torquebox.web");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        final ModuleSpecification moduleSpecification = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        if (unit.hasAttachment( RackApplicationMetaData.ATTACHMENT_KEY )) {
            addDependency( moduleSpecification, moduleLoader, TORQUEBOX_WEB_ID );
        }
    }

    private void addDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader, ModuleIdentifier moduleIdentifier) {
        moduleSpecification.addDependency( new ModuleDependency( moduleLoader, moduleIdentifier, false, false, false ) );
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

}
