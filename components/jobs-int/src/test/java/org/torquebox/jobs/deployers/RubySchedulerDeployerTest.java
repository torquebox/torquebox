package org.torquebox.jobs.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.core.RubyScheduler;
import org.torquebox.jobs.metadata.RubyJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class RubySchedulerDeployerTest extends AbstractDeployerTestCase {
	
	private RubySchedulerDeployer deployer;

	@Before
	public void setUp() throws Throwable {
		this.deployer = new RubySchedulerDeployer();
		addDeployer( this.deployer );
	}
	
	
	/** Ensure that given no jobs, a scheduler is not deployed. */
	@Test
	public void testNoJobsNoScheduler() throws Exception {
		
		String deploymentName = createDeployment( "no-jobs" );
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		
		processDeployments( true );
		
		String schedulerBeanName = AttachmentUtils.beanName( unit, RubyScheduler.class );
		BeanMetaData schedulerMetaData = getBeanMetaData(unit, schedulerBeanName);
		assertNull( schedulerMetaData );
		
		undeploy( deploymentName );
	}
	
	
	/** Ensure that given at least one job, a scheduler is deployed. */
	@Test
	public void testSchedulerDeployment() throws Exception {
		
		String deploymentName = createDeployment( "with-jobs" );
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		
		RubyJobMetaData jobMeta = new RubyJobMetaData();
		AttachmentUtils.multipleAttach(unit, jobMeta, "job.one" );
	
		processDeployments( true );
		
		String schedulerBeanName = AttachmentUtils.beanName( unit, RubyScheduler.class );
		BeanMetaData schedulerMetaData = getBeanMetaData(unit, schedulerBeanName);
		assertNotNull( schedulerMetaData );
		
		RubyScheduler scheduler = (RubyScheduler) getBean( schedulerBeanName );
		assertNotNull( scheduler );
		
		undeploy( deploymentName );
	}
	
	
}