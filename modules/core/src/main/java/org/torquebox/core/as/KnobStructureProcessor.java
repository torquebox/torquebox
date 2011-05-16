package org.torquebox.core.as;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleSpecification;

public class KnobStructureProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        if (!unit.getName().endsWith( ".knob" )) {
            return;
        }
        
        KnobDeploymentMarker.applyMark( unit );
        
        ModuleSpecification moduleSpec = unit.getAttachment( Attachments.MODULE_SPECIFICATION );
        moduleSpec.setChildFirst( true );

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }

}
