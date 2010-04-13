package org.torquebox.rack.deployers;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RubyRackApplicationFactory;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class RubyRackApplicationFactoryDeployerTest extends AbstractDeployerTestCase {

	private RubyRackApplicationFactoryDeployer deployer;
	private String runtimeInstanceFactoryDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RubyRackApplicationFactoryDeployer();
		addDeployer(this.deployer);
	}

	@Test
	public void testDefaultPool() throws Exception {
		JavaArchive archive = createJar( "runtime-factory" );
		archive.addResource(getClass().getResource("runtime-factory-jboss-beans.xml"), "jboss-beans.xml");
		File archiveFile = createJarFile( archive );
		this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );

		String deploymentName = createDeployment("shared");
		DeploymentUnit unit = getDeploymentUnit(deploymentName);

		
		RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
		rackAppMetaData.setRubyRuntimeFactoryName( "runtime-factory" );
		
		unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
		
		PoolMetaData poolMetaData = new PoolMetaData();
		poolMetaData.setName("web");
		poolMetaData.setShared();

		unit.addAttachment(PoolMetaData.class, poolMetaData);
		
		processDeployments(true);

		String beanName = AttachmentUtils.beanName(unit, RubyRackApplicationFactory.class);
		
		BeanMetaData bmd = getBeanMetaData(unit, beanName);
		assertNotNull( bmd );
		
		RubyRackApplicationFactory factory = (RubyRackApplicationFactory) getBean(beanName);
		assertNotNull( factory );
		
		assertNotNull( factory.getRubyRuntimeFactory() );
		
		undeploy( deploymentName );
		undeploy( this.runtimeInstanceFactoryDeploymentName );

	}

}
