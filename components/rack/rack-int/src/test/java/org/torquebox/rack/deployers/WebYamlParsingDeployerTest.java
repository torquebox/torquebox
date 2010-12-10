package org.torquebox.rack.deployers;

import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class WebYamlParsingDeployerTest extends AbstractDeployerTestCase {
	
	private WebYamlParsingDeployer deployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new WebYamlParsingDeployer();
		addDeployer( this.deployer );
	}
	
	@Test(expected=DeploymentException.class)
	public void testEmptyWebYml() throws Exception {
        URL appRackYml = getClass().getResource("empty-web.yml");
        
        String deploymentName = addDeployment(appRackYml, "web.yml");
        processDeployments(true);
	}
	
	@Test
	public void testValidWebYml() throws Exception {
		URL appRackYml = getClass().getResource( "valid-web.yml" );
		
		String deploymentName = addDeployment( appRackYml, "web.yml" );
		processDeployments(true);
		
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );
        
        assertNotNull( rackMetaData );
        
        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get(0) );
        assertNull( rackMetaData.getStaticPathPrefix() );
	}
	
	@Test
	public void testValidWebYmlCustomStaticPathPrefix() throws Exception {
		URL appRackYml = getClass().getResource( "static-path-web.yml" );
		
		String deploymentName = addDeployment( appRackYml, "web.yml" );
		processDeployments(true);
		
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.class );
        
        assertNotNull( rackMetaData );
        
        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get(0) );
        assertEquals( "/elsewhere", rackMetaData.getStaticPathPrefix() );
	}
}
