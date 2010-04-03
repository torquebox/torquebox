package org.torquebox.interp.deployers;

import java.io.File;
import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.core.DefaultRubyRuntimePool;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class PoolDeployerTest extends AbstractDeployerTestCase {

	private PoolDeployer deployer;
	private String runtimeInstanceFactoryDeploymentName;
	private String runtimeInstanceDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new PoolDeployer();
		addDeployer(this.deployer);
	}

	@Test
	public void testMinMaxPool() throws Exception {
		
		RubyRuntimeFactory runtimeFactory = deployRuntimeInstanceFactory();
		
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
		undeployRuntimeInstanceFactory();
	}
	
	@Test
	public void testSharedPoolWithFactory() throws Exception {
		
		RubyRuntimeFactory runtimeFactory = deployRuntimeInstanceFactory();
		
		String deploymentName = createDeployment("shared");

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
		undeployRuntimeInstanceFactory();
	}
	
	@Test
	public void testGlobalPoolWithRuntimeInstanceBean() throws Exception {
		
		deployRuntimeInstanceFactory();
		Ruby ruby = deployRuntimeInstance();
		
		String deploymentName = createDeployment("global");

		PoolMetaData poolMetaData = new PoolMetaData();
		poolMetaData.setInstanceName("runtime_instance");
		poolMetaData.setName("pool_one");
		poolMetaData.setGlobal();

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, "pool", "pool_one");

		processDeployments(true);
		SharedRubyRuntimePool poolOne = (SharedRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertSame( ruby, poolOne.getInstance() );

		undeploy(deploymentName);
		undeployRuntimeInstance();
		undeployRuntimeInstanceFactory();
	}
	
	protected RubyRuntimeFactory deployRuntimeInstanceFactory() throws IOException, DeploymentException {
		JavaArchive archive = createJar( "instance_factory" );

		archive.addResource(getClass().getResource("instance-factory-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		RubyRuntimeFactory runtimeFactory = (RubyRuntimeFactory) getBean( "instance_factory" );
		assertNotNull( runtimeFactory );
		return runtimeFactory;
	}
	
	protected void undeployRuntimeInstanceFactory() throws DeploymentException {
		undeploy( this.runtimeInstanceFactoryDeploymentName );
	}
	
	protected Ruby deployRuntimeInstance() throws IOException, DeploymentException {
		JavaArchive archive = createJar( "runtime_instance" );

		archive.addResource(getClass().getResource("runtime-instance-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		this.runtimeInstanceDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		Ruby ruby = (Ruby) getBean( "runtime_instance" );
		assertNotNull( ruby );
		return ruby;
	}
	
	protected void undeployRuntimeInstance() throws DeploymentException {
		undeploy( this.runtimeInstanceDeploymentName );
	}

}
