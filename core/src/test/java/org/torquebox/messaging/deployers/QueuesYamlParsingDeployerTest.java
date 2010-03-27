package org.torquebox.messaging.deployers;

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Test;
import org.torquebox.AbstractDeployerTestCase;
import org.torquebox.messaging.core.QueuesYamlParsingDeployer;
import org.torquebox.messaging.metadata.QueueMetaData;

import static org.junit.Assert.*;

public class QueuesYamlParsingDeployerTest extends AbstractDeployerTestCase {

	@Test
	public void testEmptyQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("empty-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		assertTrue( unit.getAllMetaData( QueueMetaData.class ).isEmpty() );
	}

	@Test
	public void testJunkQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("junk-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		assertTrue( unit.getAllMetaData( QueueMetaData.class ).isEmpty() );
	}
	
	@Test
	public void testSimpleQueuesYml() throws Throwable {
		QueuesYamlParsingDeployer deployer = new QueuesYamlParsingDeployer();
		addDeployer(deployer);

		URL queuesYml = getClass().getResource("simple-queues.yml");
		
		String deploymentName = addDeployment(queuesYml, "queues.yml");
		processDeployments(true);
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		
		Set<? extends QueueMetaData> queues = unit.getAllMetaData( QueueMetaData.class );
		assertEquals( 2, queues.size() );
		
		assertHasQueue( "queueOne", queues );
		assertHasQueue( "queueTwo", queues );
	}
	
	protected void assertHasQueue(String name, Set<? extends QueueMetaData> queues) {
		QueueMetaData queue = findQueue(name, queues);
		
		assertNotNull( "Unable to find queue: " + name, queue );
	}
	
	protected QueueMetaData findQueue(String name, Set<? extends QueueMetaData> queues) {
		for ( QueueMetaData queue : queues ) {
			if ( queue.getName().equals( name ) ) {
				return queue;
			}
		}
		
		return null;
	}
	

}
