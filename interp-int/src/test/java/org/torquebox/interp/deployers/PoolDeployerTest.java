package org.torquebox.interp.deployers;

import java.io.File;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.core.DefaultRubyRuntimePool;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class PoolDeployerTest extends AbstractDeployerTestCase {

	private PoolDeployer deployer;
	private String injectableDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new PoolDeployer();
		addDeployer(this.deployer);
	}

	/*
	@Before
	public void setUpInjectables() throws Throwable {
		this.injectableDeploymentName = addDeployment(getClass().getResource("pool-deployer-jboss-beans.xml"));
	}

	@After
	public void tearDownInjectables() throws Throwable {
		undeploy(this.injectableDeploymentName);
	}
	*/

	@Test
	public void testMinMaxPool() throws Exception {
		JavaArchive archive = createJar( "testMinMaxPool" );

		archive.addResource(getClass().getResource("pool-deployer-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		String injectableDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		
		RubyRuntimeFactory runtimeFactory = (RubyRuntimeFactory) getBean( "instance_factory" );
		assertNotNull( runtimeFactory );
		
		String deploymentName = createDeployment("minMax");

		PoolMetaData poolMetaData = new PoolMetaData();
		poolMetaData.setInstanceFactoryName("instance_factory");
		poolMetaData.setName("pool_one");
		poolMetaData.setMinimumSize(10);
		poolMetaData.setMaximumSize(200);

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, "pool", "pool_one");

		processDeployments(true);
		DefaultRubyRuntimePool poolOne = (DefaultRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertEquals( 10, poolOne.getMinInstances() );
		assertEquals( 200, poolOne.getMaxInstances() );
		assertSame( runtimeFactory, poolOne.getRubyRuntimeFactory() );

		undeploy(deploymentName);
		undeploy(injectableDeploymentName);	
	}
	
	@Test
	public void testGlobalPoolWithFactory() throws Exception {
		JavaArchive archive = createJar( "testGlobalPool" );

		archive.addResource(getClass().getResource("pool-deployer-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		String injectableDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		
		RubyRuntimeFactory runtimeFactory = (RubyRuntimeFactory) getBean( "instance_factory" );
		assertNotNull( runtimeFactory );
		
		String deploymentName = createDeployment("minMax");

		PoolMetaData poolMetaData = new PoolMetaData();
		poolMetaData.setInstanceFactoryName("instance_factory");
		poolMetaData.setName("pool_one");
		poolMetaData.setShared();

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, "pool", "pool_one");

		processDeployments(true);
		SharedRubyRuntimePool poolOne = (SharedRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertSame( runtimeFactory, poolOne.getRubyRuntimeFactory() );

		undeploy(deploymentName);
		undeploy(injectableDeploymentName);	
	}

}
