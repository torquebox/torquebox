package org.torquebox.core.as;

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
import org.torquebox.core.app.RubyApplicationMetaData;

public class CoreDependenciesProcessor implements DeploymentUnitProcessor {
    
    private static ModuleIdentifier TORQUEBOX_BOOTSTRAP_ID = ModuleIdentifier.create("org.torquebox.bootstrap");
    private static ModuleIdentifier TORQUEBOX_CORE_ID = ModuleIdentifier.create("org.torquebox.core");
    private static ModuleIdentifier JBOSS_VFS_ID = ModuleIdentifier.create("org.jboss.vfs");
    private static ModuleIdentifier JBOSS_MSC_ID = ModuleIdentifier.create("org.jboss.msc");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        final ModuleSpecification moduleSpecification = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        if (unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY )) {
            //addDependency( moduleSpecification, moduleLoader, TORQUEBOX_BOOTSTRAP_ID );
            addDependency( moduleSpecification, moduleLoader, TORQUEBOX_CORE_ID );
            addDependency( moduleSpecification, moduleLoader, JBOSS_VFS_ID );
            addDependency( moduleSpecification, moduleLoader, JBOSS_MSC_ID );
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
