package org.torquebox.rack.deployers;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.deployers.PoolingYamlParsingDeployer;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.rack.core.RackApplicationPoolImpl;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rack.spi.RackApplicationPool;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;
import static org.junit.Assert.*;


public class RackApplicationPoolDeployerTest extends AbstractDeployerTestCase {

	private RackApplicationPoolDeployer deployer;
	private String supportDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RackApplicationPoolDeployer();
		addDeployer(this.deployer);
	}

	@Test
	public void testDefaultPool() throws Exception {
		JavaArchive archive = createJar( "runtime-factory" );
		archive.addResource(getClass().getResource("runtime-factory-jboss-beans.xml"), "runtime-factory-jboss-beans.xml");
		archive.addResource(getClass().getResource("runtime-pool-jboss-beans.xml"), "runtime-pool-jboss-beans.xml");
		archive.addResource(getClass().getResource("rack-factory-jboss-beans.xml"), "rack-factory-jboss-beans.xml");
		File archiveFile = createJarFile( archive );
		
		this.supportDeploymentName = addDeployment( archiveFile );
		processDeployments(true);

		String deploymentName = createDeployment("shared");
		DeploymentUnit unit = getDeploymentUnit(deploymentName);

		RackApplicationMetaData rackAppMetaData = new RackApplicationMetaData();
		rackAppMetaData.setRackApplicationFactoryName( "rack-factory" );
		rackAppMetaData.setRubyRuntimePoolName( "runtime-pool" );
		unit.addAttachment( RackApplicationMetaData.class, rackAppMetaData );
		
		processDeployments(true);

		String beanName = AttachmentUtils.beanName(unit, RackApplicationPool.class);
		
		assertEquals( rackAppMetaData.getRackApplicationPoolName(), beanName );
		
		BeanMetaData bmd = getBeanMetaData(unit, beanName);
		assertNotNull( bmd );
		
		RackApplicationPoolImpl pool = (RackApplicationPoolImpl) getBean(beanName);
		assertNotNull( pool );
		
		undeploy( deploymentName );
		undeploy( this.supportDeploymentName );

	}

	@Test
	public void testPoolMetaDataAttaching() throws Throwable {
		String deploymentName = createDeployment("test");
		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment( RackApplicationMetaData.class, new RackApplicationMetaData() );

		processDeployments(false);

        PoolMetaData poolMetaData = AttachmentUtils.getAttachment( unit, "web", PoolMetaData.class );
        assertNotNull( poolMetaData );
        assertTrue( poolMetaData.isShared() );

        AttachmentUtils.multipleAttach(unit, new PoolMetaData("web", 2, 4), "web");
        processDeployments(false);

        poolMetaData = AttachmentUtils.getAttachment( unit, "web", PoolMetaData.class );
        assertNotNull( poolMetaData );
        assertFalse( poolMetaData.isShared() );
        assertEquals( 2, poolMetaData.getMinimumSize() );
        assertEquals( 4, poolMetaData.getMaximumSize() );
	}

	@Test
	public void testPoolingYamlOverride() throws Throwable {
        addDeployer( new PoolingYamlParsingDeployer() );
		JavaArchive archive = createJar( "test" );
		archive.addResource(getClass().getResource("pooling.yml"), "/META-INF/pooling.yml");
		File archiveFile = createJarFile( archive );
		
		String deploymentName = addDeployment( archiveFile );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( RackApplicationMetaData.class, new RackApplicationMetaData() );
		processDeployments(false);

        PoolMetaData poolMetaData = AttachmentUtils.getAttachment( unit, "web", PoolMetaData.class );
        assertNotNull( poolMetaData );
        assertFalse( poolMetaData.isShared() );
        assertEquals( 2, poolMetaData.getMinimumSize() );
        assertEquals( 4, poolMetaData.getMaximumSize() );
	}
}
