package org.torquebox.jobs.deployers;

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

public class ServicesYamlParsingDeployerTest extends AbstractDeployerTestCase {
	
	private ServicesYamlParsingDeployer deployer;

	@Before
	public void setUp() throws Throwable {
		this.deployer = new ServicesYamlParsingDeployer();
		addDeployer( this.deployer );
	}
	
	
	/** Ensure that an empty services.yml causes no problems. */
	@Test
	public void testEmptyServicesYml() throws Exception {
        URL servicesYml = getClass().getResource("empty.yml");
        
        String deploymentName = addDeployment(servicesYml, "services.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        assertNotNull( unit );
        
	}
	
	/** Ensure that a valid services.yml attaches metadata. */
    @Ignore
	@Test
	public void testValidServicesYml() throws Exception {
        URL servicesYml = getClass().getResource("valid-services.yml");
        
        String deploymentName = addDeployment(servicesYml, "services.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        assertNotNull( unit );
	}
	
}
