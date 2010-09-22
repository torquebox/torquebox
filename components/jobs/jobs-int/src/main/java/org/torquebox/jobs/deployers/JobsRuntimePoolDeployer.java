package org.torquebox.jobs.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.metadata.EnvironmentMetaData;

public class JobsRuntimePoolDeployer extends AbstractDeployer {
    
    public JobsRuntimePoolDeployer() {
        setStage( DeploymentStages.DESCRIBE );
        addInput( EnvironmentMetaData.class );
        addInput( PoolMetaData.class );
        addOutput( PoolMetaData.class );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if ( unit.getAllMetaData( ScheduledJobMetaData.class ).isEmpty() ) {
            return;
        }
        
        Set<? extends PoolMetaData> allPools = unit.getAllMetaData( PoolMetaData.class );
        
        PoolMetaData jobsPool = null;
        
        for ( PoolMetaData each : allPools ) {
            if ( each.getName().equals( "jobs" ) ) {
                jobsPool = each;
                break;
            }
        }
        
        if ( jobsPool == null ) {
            EnvironmentMetaData envMetaData = unit.getAttachment(EnvironmentMetaData.class);
            boolean devMode = envMetaData != null && envMetaData.isDevelopmentMode();
            jobsPool = devMode ? new PoolMetaData("jobs", 1, 2) : new PoolMetaData("jobs");
            log.info("Configured Ruby runtime pool for jobs: " + jobsPool);
            AttachmentUtils.multipleAttach(unit, jobsPool, "jobs");
        }
    }

}
