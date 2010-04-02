package org.torquebox.interp.deployers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedDeployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.core.DefaultRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class PoolDeployerTest extends AbstractDeployerTestCase {
	
	private PoolDeployer deployer;
	private String injectableDeploymentName;
	
	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new PoolDeployer();
		addDeployer( this.deployer );
	}
	
	@Before
	public void setUpInjectables() throws Throwable {
		this.injectableDeploymentName = addDeployment( getClass().getResource( "pool-deployer-jboss-beans.xml") );
	}
	
	@After
	public void tearDownInjectables() throws Throwable {
		undeploy( this.injectableDeploymentName );
	}
	
	
	@Test
	public void testMinMaxPool() throws Exception {
		String deploymentName = createDeployment("minMax");
		
		
		PoolMetaData poolMetaData = new PoolMetaData();
		poolMetaData.setInstanceFactoryName( "instance_factory" );
		poolMetaData.setName( "pool_one" );
		poolMetaData.setMinimumSize(10);
		poolMetaData.setMaximumSize(200);
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment(PoolMetaData.class, poolMetaData);
		
		String beanName = AttachmentUtils.beanName(unit, "pool", "pool_one" );
		
		processDeployments(true);
		DefaultRubyRuntimePool poolOne = (DefaultRubyRuntimePool) getKernelController().getKernel().getRegistry().findEntry( beanName ).getTarget();
		assertNotNull( poolOne );
		assertNotNull( poolOne.getRubyRuntimeFactory() );
		
		undeploy( deploymentName );
	}

}
