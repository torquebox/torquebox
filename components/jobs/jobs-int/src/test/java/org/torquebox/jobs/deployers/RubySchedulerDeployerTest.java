package org.torquebox.jobs.deployers;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.core.RubyScheduler;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class RubySchedulerDeployerTest extends AbstractDeployerTestCase {
	
	private RubySchedulerDeployer deployer;

	@Before
	public void setUp() throws Throwable {
		this.deployer = new RubySchedulerDeployer();
		this.deployer.setRubyRuntimePoolName( "runtime-pool" );
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
		JavaArchive archive = createJar( "scheduler-support" );
		archive.addResource(getClass().getResource("interp-jboss-beans.xml"), "interp-jboss-beans.xml");
		File archiveFile = createJarFile( archive );
		
		String supportDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		
		String deploymentName = createDeployment( "with-jobs" );
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		
		ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
		AttachmentUtils.multipleAttach(unit, jobMeta, "job.one" );
	
		processDeployments( true );
		
		String schedulerBeanName = AttachmentUtils.beanName( unit, RubyScheduler.class );
		BeanMetaData schedulerMetaData = getBeanMetaData(unit, schedulerBeanName);
		assertNotNull( schedulerMetaData );
		
		RubyScheduler scheduler = (RubyScheduler) getBean( schedulerBeanName );
		assertNotNull( scheduler );
		
		undeploy( deploymentName );
		undeploy( supportDeploymentName );
	}
	
	
}