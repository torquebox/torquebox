package org.torquebox.jobs.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.base.metadata.EnvironmentMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: DESCRIBE
 *    In: EnvironmentMetaData, PoolMetaData, ScheduledJobMetaData
 *   Out: PoolMetaData
 * </pre>
 *
 * Ensures that pool metadata for jobs is available
 */
public class JobsRuntimePoolDeployer extends AbstractDeployer {
    
    /**
     * I'd rather use setInput(ScheduledJobMetaData) and omit the
     * getAllMetaData short circuit in deploy(), but that requires
     * attachers to pass an ExpectedType, and I don't think we can
     * assume that.
     */
    public JobsRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( ScheduledJobMetaData.class );
        addInput( EnvironmentMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if ( unit.getAllMetaData( ScheduledJobMetaData.class ).isEmpty() ) {
            return;
        }
        PoolMetaData jobsPool = AttachmentUtils.getAttachment( unit, "jobs", PoolMetaData.class );;
        if ( jobsPool == null ) {
            EnvironmentMetaData envMetaData = unit.getAttachment(EnvironmentMetaData.class);
            boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
            jobsPool = devMode ? new PoolMetaData("jobs", 1, 2) : new PoolMetaData("jobs");
            log.info("Configured Ruby runtime pool for jobs: " + jobsPool);
            AttachmentUtils.multipleAttach(unit, jobsPool, "jobs");
        }
    }

}
