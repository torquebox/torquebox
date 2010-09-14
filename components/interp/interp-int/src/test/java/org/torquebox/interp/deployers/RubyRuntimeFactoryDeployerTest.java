package org.torquebox.interp.deployers;

import static org.junit.Assert.*;

import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubyRuntimeFactoryDeployerTest extends AbstractDeployerTestCase {
	

	private RubyRuntimeFactoryDeployer deployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RubyRuntimeFactoryDeployer();
		this.deployer.setKernel( getKernelController().getKernel() );
		addDeployer(this.deployer);
	}
	
	@Test
	public void testBasics() {
		assertSame( DeploymentStages.CLASSLOADER, this.deployer.getStage() );
		assertSame( RubyRuntimeMetaData.class, this.deployer.getInput() );
		assertTrue( this.deployer.getOutputs().contains( Ruby.class.getName() ) );
	}
	
	@Test
	public void testDeployment() throws Exception {
		String deploymentName = createDeployment("runtimeFactory");
		
		RubyRuntimeMetaData metaData = new RubyRuntimeMetaData();
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( RubyRuntimeMetaData.class, metaData );
		
		processDeployments(true);
		
		RubyRuntimeFactory factory = (RubyRuntimeFactory) getBean( AttachmentUtils.beanName( unit, RubyRuntimeFactory.class ) );
		
		assertNotNull(factory);
		assertEquals( CompatVersion.RUBY1_8, factory.getRubyVersion() );
		
		undeploy( deploymentName );
	}
	
	@Test
	public void testDeploymentWithNonDefaultRubyCompatibilityVersion() throws Exception {
		String deploymentName = createDeployment("runtimeFactory");
		
		RubyRuntimeMetaData metaData = new RubyRuntimeMetaData();
		metaData.setVersion( RubyRuntimeMetaData.Version.V1_9 );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( RubyRuntimeMetaData.class, metaData );
		
		processDeployments(true);
		
		RubyRuntimeFactory factory = (RubyRuntimeFactory) getBean( AttachmentUtils.beanName( unit, RubyRuntimeFactory.class ) );
		
		assertNotNull(factory);
		assertEquals( CompatVersion.RUBY1_9, factory.getRubyVersion() );
		
		undeploy( deploymentName );
	}

	
}
