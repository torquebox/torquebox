package org.torquebox.hasingleton;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.torquebox.core.processors.ClusterAwareProcessor;

public class HASingletonInstaller extends ClusterAwareProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        log.info( "Deploying HA-Singleton" );
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        ServiceBuilder<Void> builder = phaseContext.getServiceTarget().addService( HASingleton.serviceName( unit ), new HASingleton() );

        if (isClustered( phaseContext )) {
            log.info( "Clustered, not activating" );
            builder.setInitialMode( Mode.NEVER );
        } else {
            log.info( "Not clustered, activating" );
            builder.setInitialMode( Mode.ACTIVE );
        }

        ServiceController<Void> singletonController = builder.install();

        if (isClustered( phaseContext )) {
            log.info( "Clustered, deploying coordinator" );
            HASingletonCoordinatorService coordinator = new HASingletonCoordinatorService( singletonController, unit.getName() );

            phaseContext.getServiceTarget().addService( HASingleton.serviceName( unit ).append( "coordinator" ), coordinator )
                    .addDependency( ChannelFactoryService.getServiceName( null ), ChannelFactory.class, coordinator.getChannelFactoryInjector() )
                    .install();
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        // TODO Auto-generated method stub

    }

    private static final Logger log = Logger.getLogger( "org.torquebox.hasingleton" );

}
