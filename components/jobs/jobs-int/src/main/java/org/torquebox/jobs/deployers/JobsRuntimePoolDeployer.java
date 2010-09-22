package org.torquebox.jobs.deployers;

import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;

public class JobsRuntimePoolDeployer extends AbstractDeployer {
	
	public JobsRuntimePoolDeployer() {
		setStage( DeploymentStages.DESCRIBE );
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
			jobsPool = new PoolMetaData("jobs", 1, 2);
			AttachmentUtils.multipleAttach(unit, jobsPool, "jobs");
		}
	}

}
