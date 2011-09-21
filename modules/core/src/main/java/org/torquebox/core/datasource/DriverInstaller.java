package org.torquebox.core.datasource;

import java.io.IOException;
import java.util.List;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.connector.registry.DriverRegistry;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.as.CoreServices;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class DriverInstaller implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        
        if ( rubyAppMetaData == null ) {
            return;
        }

        List<DriverMetaData> allMetaData = unit.getAttachmentList( DriverMetaData.ATTACHMENTS );

        try {
            String applicationDir = rubyAppMetaData.getRoot().getPhysicalFile().getAbsolutePath();
            for (DriverMetaData each : allMetaData) {
                DriverService driverService = new DriverService( applicationDir, each.getDriverId(), each.getDriverClassName() );

                ServiceName name = DataSourceServices.driverName( unit, each.getDriverId() );

                phaseContext
                        .getServiceTarget()
                        .addService( name, driverService )
                        .addDependency( ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE, DriverRegistry.class, driverService.getDriverRegistryInjector() )
                        .addDependency( CoreServices.runtimeFactoryName( unit ).append( "lightweight" ), RubyRuntimeFactory.class,
                                driverService.getRuntimeFactoryInjector() )
                        .setInitialMode( ServiceController.Mode.ACTIVE ).install();
            }
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }

    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }
}