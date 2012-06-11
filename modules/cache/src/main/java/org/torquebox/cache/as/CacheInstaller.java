package org.torquebox.cache.as;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.util.ClusterUtil;

public class CacheInstaller implements DeploymentUnitProcessor {

	public CacheInstaller() {		
	}
	
	@Override
	public void deploy(DeploymentPhaseContext phaseContext)
			throws DeploymentUnitProcessingException {
        CacheService service = new CacheService();
        service.setClustered( ClusterUtil.isClustered(phaseContext) );
        ServiceBuilder<CacheService> builder = phaseContext.getServiceTarget().addService( CacheServices.CACHE, service );
        builder.addDependency( ServiceName.JBOSS.append("infinispan", "polyglot") );
        builder.setInitialMode( Mode.ACTIVE );
        builder.install();
	}

	@Override
	public void undeploy(DeploymentUnit unit) {
		
	}
	
}
