package org.torquebox.interp.deployers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class BootstrapRubyRuntimeDeployerTest extends AbstractDeployerTestCase {
	
	private RubyRuntimeFactoryDeployer rubyRuntimeFactoryDeployer;
	private BootstrapRubyRuntimeDeployer bootstrapRubyRuntimeDeployer;

	@Before
	public void setUpDeployers() throws Throwable {
		this.rubyRuntimeFactoryDeployer = new RubyRuntimeFactoryDeployer();
		addDeployer(this.rubyRuntimeFactoryDeployer);
		this.bootstrapRubyRuntimeDeployer = new BootstrapRubyRuntimeDeployer();
		addDeployer(this.bootstrapRubyRuntimeDeployer);
		
	}
	
	@Test
	public void testBasics() {
		assertSame( DeploymentStages.CLASSLOADER, this.bootstrapRubyRuntimeDeployer.getStage() );
		assertSame( RubyRuntimeMetaData.class, this.bootstrapRubyRuntimeDeployer.getInput() );
		assertTrue( this.bootstrapRubyRuntimeDeployer.getOutputs().contains( BeanMetaData.class.getName() ) );
	}
	
	@Test
	public void testDeployment() throws Exception {
		String deploymentName = createDeployment("bootstrapRuntime");
		
		RubyRuntimeMetaData metaData = new RubyRuntimeMetaData();
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( RubyRuntimeMetaData.class, metaData );
		
		processDeployments(true);
		
		Ruby ruby = (Ruby) getBean( AttachmentUtils.beanName( unit, Ruby.class, "bootstrap" ) );
		
		assertNotNull(ruby);
		
		undeploy( deploymentName );
	}

	
}
