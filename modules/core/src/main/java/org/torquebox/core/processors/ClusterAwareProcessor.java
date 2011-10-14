package org.torquebox.core.processors;

import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;

public abstract class ClusterAwareProcessor implements DeploymentUnitProcessor {
    
    protected boolean isClustered(DeploymentPhaseContext context) {
        boolean isClustered = ( context.getServiceRegistry().getService( ChannelFactoryService.getServiceName( null ) ) != null );
        System.err.println( "IS CLUSTERED? " + isClustered );
        return isClustered;
    }

}
