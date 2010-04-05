package org.torquebox.rack.deployers;

import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.rack.metadata.RackWebApplicationMetaData;
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
        
        RackWebApplicationMetaData webMetaData = unit.getAttachment( RackWebApplicationMetaData.class );
        
        assertNotNull( webMetaData );
        
        assertEquals( "/tacos", webMetaData.getContext() );
        assertEquals( "foobar.com", webMetaData.getHost() );
        assertEquals( "/public", webMetaData.getStaticPathPrefix() );
	}
	
	@Test
	public void testValidWebYmlCustomStaticPathPrefix() throws Exception {
		URL appRackYml = getClass().getResource( "static-path-web.yml" );
		
		String deploymentName = addDeployment( appRackYml, "web.yml" );
		processDeployments(true);
		
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        RackWebApplicationMetaData webMetaData = unit.getAttachment( RackWebApplicationMetaData.class );
        
        assertNotNull( webMetaData );
        
        assertEquals( "/tacos", webMetaData.getContext() );
        assertEquals( "foobar.com", webMetaData.getHost() );
        assertEquals( "/elsewhere", webMetaData.getStaticPathPrefix() );
	}
}
