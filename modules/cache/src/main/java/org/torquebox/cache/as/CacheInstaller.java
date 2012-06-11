package org.torquebox.cache.as;

import org.infinispan.manager.CacheContainer;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.projectodd.polyglot.core.util.ClusterUtil;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.RubyRuntimePool;

public class CacheInstaller implements DeploymentUnitProcessor {

	public CacheInstaller() {		
	}
	
	@Override
	public void deploy(DeploymentPhaseContext phaseContext)
			throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        CacheService service = new CacheService();
        if (ClusterUtil.isClustered( phaseContext )) {
            log.debug( "Deploying clustered cache: " + unit );
            // Deploy clustered cache
        } else {
            log.debug( "Deploying local cache: " + unit );
            // 
        }
        ServiceName serviceName = CacheServices.CACHE;
        ServiceBuilder<CacheService> builder = phaseContext.getServiceTarget().addService( serviceName, service );
        builder.addDependency( org.jboss.as., RubyRuntimePool.class, scheduler.getRubyRuntimePoolInjector() );
        builder.setInitialMode( Mode.ACTIVE );
        builder.install();
	}

	@Override
	public void undeploy(DeploymentUnit unit) {
	}
	
    private static final Logger log = Logger.getLogger( "org.torquebox.cache" );

}
