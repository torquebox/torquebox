package org.torquebox.rack.deployers;

import java.net.URL;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.mc.vdf.PojoDeployment;
import org.torquebox.rack.core.RackRuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class AppRackYamlParsingDeployerTest extends AbstractDeployerTestCase {
	
	private AppRackYamlParsingDeployer deployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new AppRackYamlParsingDeployer();
		addDeployer( this.deployer );
	}
	
	@Test(expected=DeploymentException.class)
	public void testEmptyAppRackYml() throws Exception {
        URL appRackYml = getClass().getResource("empty-app-rack.yml");
        
        String deploymentName = addDeployment(appRackYml, "app-rack.yml");
        processDeployments(true);
	}
	
	@Test
	public void testValidAppRackYml() throws Exception {
		log.info( "BEGIN testValidAppRackYaml" );
		URL appRackYml = getClass().getResource( "valid-app-rack.yml" );
		
		String deploymentName = addDeployment( appRackYml, "app-rack.yml" );
		processDeployments(true);
		
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
		String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, "app-rack.yml" );
		BeanMetaData bmd = getBeanMetaData( unit, beanName );
		assertNotNull( bmd );
		
		PojoDeployment pojo = (PojoDeployment) getBean( beanName );
		assertNotNull( pojo );
	
		VFSDeployment deployment = pojo.getDeployment();
		
		assertEquals( "vfs:///tmp/nonexistantpathfortorqueboxtest", deployment.getRoot().toURI().toString() );
		
		Attachments attachments = deployment.getPredeterminedManagedObjects();
		
		RackApplicationMetaData rackAppMetaData = attachments.getAttachment( RackApplicationMetaData.class );
		assertNotNull( rackAppMetaData );
		assertEquals( "test", rackAppMetaData.getRackEnv() );
		
		RubyRuntimeMetaData rubyRuntimeMetaData = attachments.getAttachment( RubyRuntimeMetaData.class );
		assertNotNull( rubyRuntimeMetaData );
		assertNotNull( rubyRuntimeMetaData.getRuntimeInitializer() );
		assertTrue( rubyRuntimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer );

        assertNull( attachments.getAttachment( PoolMetaData.class ) );
		log.info( "END testValidAppRackYaml" );
	}
	
    @Test
    public void testValidAppRackYmlWithAbsolutePathToRackup() throws Exception {
        log.info("BEGIN testValidAppRackYaml");
        URL appRackYml = getClass().getResource("valid-absolute-rackup-app-rack.yml");

        String deploymentName = addDeployment(appRackYml, "app-rack.yml");
        processDeployments(true);

        DeploymentUnit unit = getDeploymentUnit(deploymentName);

        String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, "app-rack.yml");
        BeanMetaData bmd = getBeanMetaData(unit, beanName);
        assertNotNull(bmd);

        PojoDeployment pojo = (PojoDeployment) getBean(beanName);
        assertNotNull(pojo);

        VFSDeployment deployment = pojo.getDeployment();

        assertEquals("vfs:///tmp/nonexistantpathfortorqueboxtest", deployment.getRoot().toURI().toString());

        Attachments attachments = deployment.getPredeterminedManagedObjects();

        RackApplicationMetaData rackAppMetaData = attachments.getAttachment(RackApplicationMetaData.class);
        assertNotNull(rackAppMetaData);
        assertEquals("test", rackAppMetaData.getRackEnv());

        assertEquals("/path/to/config.ru", rackAppMetaData.getRackUpScriptLocation().getPathName());

        RubyRuntimeMetaData rubyRuntimeMetaData = attachments.getAttachment(RubyRuntimeMetaData.class);
        assertNotNull(rubyRuntimeMetaData);
        assertNotNull(rubyRuntimeMetaData.getRuntimeInitializer());
        assertTrue(rubyRuntimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer);

        assertNull(attachments.getAttachment(PoolMetaData.class));
    }
    
    @Test
    public void testValidAppRackYmlWithRelativePathToRackup() throws Exception {
        log.info("BEGIN testValidAppRackYaml");
        URL appRackYml = getClass().getResource("valid-relative-rackup-app-rack.yml");

        String deploymentName = addDeployment(appRackYml, "app-rack.yml");
        processDeployments(true);

        DeploymentUnit unit = getDeploymentUnit(deploymentName);

        String beanName = AttachmentUtils.beanName(unit, PojoDeployment.class, "app-rack.yml");
        BeanMetaData bmd = getBeanMetaData(unit, beanName);
        assertNotNull(bmd);

        PojoDeployment pojo = (PojoDeployment) getBean(beanName);
        assertNotNull(pojo);

        VFSDeployment deployment = pojo.getDeployment();

        assertEquals("vfs:///tmp/nonexistantpathfortorqueboxtest", deployment.getRoot().toURI().toString());

        Attachments attachments = deployment.getPredeterminedManagedObjects();

        RackApplicationMetaData rackAppMetaData = attachments.getAttachment(RackApplicationMetaData.class);
        assertNotNull(rackAppMetaData);
        assertEquals("test", rackAppMetaData.getRackEnv());

        assertEquals("/tmp/nonexistantpathfortorqueboxtest/path/to/config.ru", rackAppMetaData.getRackUpScriptLocation().getPathName());

        RubyRuntimeMetaData rubyRuntimeMetaData = attachments.getAttachment(RubyRuntimeMetaData.class);
        assertNotNull(rubyRuntimeMetaData);
        assertNotNull(rubyRuntimeMetaData.getRuntimeInitializer());
        assertTrue(rubyRuntimeMetaData.getRuntimeInitializer() instanceof RackRuntimeInitializer);

        assertNull(attachments.getAttachment(PoolMetaData.class));
    }

}
