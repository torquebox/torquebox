package org.torquebox.messaging.deployers;

import java.net.URL;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ManagedDeployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.AbstractDeployerTestCase;
import org.torquebox.messaging.metadata.QueuesMetaData;

public class QueuesYamlParsingDeployerTest extends AbstractDeployerTestCase {

	@Test
	public void testEmptyQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("empty-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Assert.assertNull( unit.getAttachment( QueuesMetaData.class ) );
	}

	@Test
	public void testSimpleQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("simple-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Assert.assertNotNull( unit.getAttachment( QueuesMetaData.class ) );
	}
	
	@Test
	public void testJunkQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("junk-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		Assert.assertNull( unit.getAttachment( QueuesMetaData.class ) );
		
	}

}
