package org.torquebox.core.processors;

import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;

public abstract class ClusterAwareProcessor implements DeploymentUnitProcessor {
    
    protected boolean isClustered(DeploymentPhaseContext context) {
        return ( context.getServiceRegistry().getService( ChannelFactoryService.getServiceName( null ) ) != null );
    }

}
