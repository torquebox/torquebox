package org.torquebox.core.injection;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.CoreServices;

public class CorePredeterminedInjectableDeployer implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        if ( ! unit.hasAttachment(  RubyApplicationMetaData.ATTACHMENT_KEY ) ) {
            return;
        }
        
        ServiceTarget serviceTarget = phaseContext.getServiceTarget();
        ServiceRegistry serviceRegistry = unit.getServiceRegistry();
        
        serviceTarget.addService(  CoreServices.serviceRegistryName( unit ), new ValueService<ServiceRegistry>( new ImmediateValue<ServiceRegistry>(serviceRegistry ) ) ).install();
        serviceTarget.addService(  CoreServices.serviceTargetName( unit ), new ValueService<ServiceTarget>( new ImmediateValue<ServiceTarget>(serviceTarget ) ) ).install();
        
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        
    }

}
