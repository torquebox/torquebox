package org.torquebox.services.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.services.ServiceMetaData;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, ScheduledJobMetaData
 *   Out: PoolMetaData
 * </pre>
 * 
 * Ensures that pool metadata for jobs is available
 */
public class ServicesRuntimePoolDeployer extends AbstractDeployer {

    public static final String POOL_NAME = "services";

    public ServicesRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( ServiceMetaData.class );
        addInput( RubyApplicationMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit.getAllMetaData( ServiceMetaData.class ).isEmpty()) {
            return;
        }

        log.debug( "Deploying ruby runtime pool for services: " + unit );
        PoolMetaData servicesPool = AttachmentUtils.getAttachment( unit, "services", PoolMetaData.class );

        if (servicesPool == null) {
            log.debug( "Configuring ruby runtime pool for services: " + unit );
            servicesPool = new PoolMetaData( "services" );
            AttachmentUtils.multipleAttach( unit, servicesPool, "services" );
        }
    }

}
