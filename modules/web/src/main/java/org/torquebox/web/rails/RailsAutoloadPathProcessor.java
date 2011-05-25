package org.torquebox.web.rails;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.torquebox.core.runtime.RubyLoadPathMetaData;
import org.torquebox.core.runtime.RubyRuntimeMetaData;

public class RailsAutoloadPathProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment( RubyRuntimeMetaData.ATTACHMENT_KEY );
        
        if (runtimeMetaData != null && runtimeMetaData.getRuntimeInitializer() instanceof RailsRuntimeInitializer) {
            RailsRuntimeInitializer initializer = (RailsRuntimeInitializer) runtimeMetaData.getRuntimeInitializer();
            for (RubyLoadPathMetaData path : runtimeMetaData.getLoadPaths()) {
                if (path.isAutoload()) {
                    initializer.addAutoloadPath( path.toString() );
                }
            }
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
