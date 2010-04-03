package org.torquebox.interp.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class RubyRuntimeFactoryDeployerTest extends AbstractDeployerTestCase {
	

	private RubyRuntimeFactoryDeployer deployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RubyRuntimeFactoryDeployer();
		addDeployer(this.deployer);
		
	}
	
	@Test
	public void testBasics() {
		assertSame( DeploymentStages.CLASSLOADER, this.deployer.getStage() );
		assertSame( RubyRuntimeMetaData.class, this.deployer.getInput() );
		assertTrue( this.deployer.getOutputs().contains( BeanMetaData.class.getName() ) );
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
		
		undeploy( deploymentName );
	}

	
}
