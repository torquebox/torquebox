package org.torquebox.messaging.as;

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

public class MessagingDependenciesProcessor implements DeploymentUnitProcessor {
    
    private static ModuleIdentifier TORQUEBOX_MESSAGING_ID = ModuleIdentifier.create("org.torquebox.messaging");
    private static ModuleIdentifier HORNETQ_ID = ModuleIdentifier.create("org.hornetq");
    private static ModuleIdentifier JAVAX_JMS_ID = ModuleIdentifier.create("javax.jms.api");
    private static ModuleIdentifier JBOSS_VFS_ID = ModuleIdentifier.create("org.jboss.vfs");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        final ModuleSpecification moduleSpecification = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        System.err.println( "Should we add messaging deps?" );
        if (unit.hasAttachment( RubyApplicationMetaData.ATTACHMENT_KEY )) {
            System.err.println( "YES!" );
            addDependency( moduleSpecification, moduleLoader, TORQUEBOX_MESSAGING_ID );
            addDependency( moduleSpecification, moduleLoader, JAVAX_JMS_ID );
            addDependency( moduleSpecification, moduleLoader, HORNETQ_ID );
            addDependency( moduleSpecification, moduleLoader, JBOSS_VFS_ID );
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
