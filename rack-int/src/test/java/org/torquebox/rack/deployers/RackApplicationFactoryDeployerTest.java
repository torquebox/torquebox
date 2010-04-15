package org.torquebox.rack.deployers;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RackApplicationFactoryImpl;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class RackApplicationFactoryDeployerTest extends AbstractDeployerTestCase {

	private RackApplicationFactoryDeployer deployer;
	private String runtimeInstanceFactoryDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RackApplicationFactoryDeployer();
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
		unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
		
		processDeployments(true);

		String beanName = AttachmentUtils.beanName(unit, RackApplicationFactoryImpl.class);
		
		BeanMetaData bmd = getBeanMetaData(unit, beanName);
		assertNotNull( bmd );
		
		RackApplicationFactoryImpl factory = (RackApplicationFactoryImpl) getBean(beanName);
		assertNotNull( factory );
		
		undeploy( deploymentName );
		undeploy( this.runtimeInstanceFactoryDeploymentName );

	}

}
