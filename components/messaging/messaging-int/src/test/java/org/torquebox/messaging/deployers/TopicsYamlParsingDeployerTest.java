package org.torquebox.messaging.deployers;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Set;
import java.io.File;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.messaging.metadata.TopicMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class TopicsYamlParsingDeployerTest extends AbstractDeployerTestCase {
	
	private TopicsYamlParsingDeployer deployer;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new TopicsYamlParsingDeployer();
		addDeployer( this.deployer );
	}
	
	@Test
	public void testEmptyYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/empty-topics.yml" );
		String deploymentName = addDeployment(config.toURI().toURL(), "topics.yml");
		
		processDeployments( true );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );
		
		assertTrue( allMetaData.isEmpty() );
		
		undeploy( deploymentName );
	}
	
	@Test
	public void testJunkYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/junk-topics.yml" );
		String deploymentName = addDeployment(config.toURI().toURL(), "topics.yml");
		
		processDeployments( true );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );
		
		assertTrue( allMetaData.isEmpty() );
		
		undeploy( deploymentName );
	}
	
	@Test
	public void testValidYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/valid-topics.yml" );
		String deploymentName = addDeployment(config.toURI().toURL(), "topics.yml");
		
		processDeployments( true );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );
		
		assertFalse( allMetaData.isEmpty() );	
		
		assertEquals( 2, allMetaData.size() );
		
		TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/foo" );
		assertNotNull( topicFoo );
		
		TopicMetaData topicBar = getMetaData( allMetaData, "/topics/bar" );
		assertNotNull( topicBar );
		
		undeploy( deploymentName );
	}

	private TopicMetaData getMetaData(Set<? extends TopicMetaData> allMetaData, String name) {
		for ( TopicMetaData each : allMetaData ) {
			if ( each.getName().equals( name ) ) {
				return each;
			}
		}
		
		return null;
	}

}
