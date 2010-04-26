package org.torquebox.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.metadata.AbstractSubsystemConfiguration;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class BasicSubsystemConfigurationDeployerTest extends AbstractDeployerTestCase {
	
	private BasicSubsystemConfigurationDeployer deployerOne;
	private ArrayList<String> loadPathsOne;
	
	private BasicSubsystemConfigurationDeployer deployerTwo;
	private ArrayList<String> loadPathsTwo;

	@Before
	public void setUp() throws Throwable {
		this.deployerOne = new BasicSubsystemConfigurationDeployer();
		
		this.deployerOne.setTriggerAttachmentName( TriggerAttachmentOne.class.getName() );
		this.deployerOne.setConfigurationClassName( MockConfigurationOne.class.getName() );
		this.loadPathsOne = new ArrayList<String>();
		this.loadPathsOne.add( "app/some-subsystem" );
		this.deployerOne.setLoadPaths( this.loadPathsOne );
		
		addDeployer( this.deployerOne, "subsystem-configurator-1" );
		
		this.deployerTwo = new BasicSubsystemConfigurationDeployer();
		
		this.deployerTwo.setTriggerAttachmentName( TriggerAttachmentTwo.class.getName() );
		this.deployerTwo.setConfigurationClassName( MockConfigurationTwo.class.getName() );
		this.loadPathsTwo = new ArrayList<String>();
		this.loadPathsTwo.add( "app/some-other-subsystem" );
		this.deployerTwo.setLoadPaths( this.loadPathsTwo );
		
		addDeployer( this.deployerTwo, "subsystem-configurator-2" );
	}
	
	@Test
	public void testWithoutTrigger() throws Exception {
		String deploymentName = createDeployment( "without-trigger" );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		
		processDeployments( true );
		
		assertTrue( unit.getAllMetaData( MockConfigurationOne.class ).isEmpty() );
		assertTrue( unit.getAllMetaData( AbstractSubsystemConfiguration.class ).isEmpty() );
	}
	
	@Test
	public void testWithTrigger() throws Exception {
		String deploymentName = createDeployment( "with-trigger" );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( TriggerAttachmentOne.class, new TriggerAttachmentOne() );
		
		processDeployments( true );
		
		assertFalse( unit.getAllMetaData( MockConfigurationOne.class ).isEmpty() );
		assertTrue( unit.getAllMetaData( MockConfigurationTwo.class ).isEmpty() );
		assertFalse( unit.getAllMetaData( AbstractSubsystemConfiguration.class ).isEmpty() );
		
		Set<? extends MockConfigurationOne> configurations = unit.getAllMetaData( MockConfigurationOne.class );
		
		assertEquals( 1, configurations.size() );
		
		MockConfigurationOne config = configurations.iterator().next();
		
		assertNotNull( config );
		
		List<String> loadPaths = config.getLoadPaths();
		
		assertNotNull( loadPaths );
		assertEquals( 1, loadPaths.size() );
		assertEquals( "app/some-subsystem", loadPaths.get(0) );
	}
	
	@Test
	public void testWithMultipleTriggers() throws Exception {
		String deploymentName = createDeployment( "with-multiple-triggers" );
		
		DeploymentUnit unit = getDeploymentUnit( deploymentName );
		unit.addAttachment( TriggerAttachmentOne.class, new TriggerAttachmentOne() );
		unit.addAttachment( TriggerAttachmentTwo.class, new TriggerAttachmentTwo() );
		
		processDeployments( true );
		
		assertFalse( unit.getAllMetaData( MockConfigurationOne.class ).isEmpty() );
		assertFalse( unit.getAllMetaData( MockConfigurationTwo.class ).isEmpty() );
		assertFalse( unit.getAllMetaData( AbstractSubsystemConfiguration.class ).isEmpty() );
		
		Set<? extends MockConfigurationOne> configurationsOne = unit.getAllMetaData( MockConfigurationOne.class );
		assertEquals( 1, configurationsOne.size() );
		MockConfigurationOne configOne = configurationsOne.iterator().next();
		assertNotNull( configOne );
		List<String> loadPathsOne = configOne.getLoadPaths();
		assertNotNull( loadPathsOne );
		assertEquals( 1, loadPathsOne.size() );
		assertEquals( "app/some-subsystem", loadPathsOne.get(0) );
		
		Set<? extends MockConfigurationTwo> configurationsTwo = unit.getAllMetaData( MockConfigurationTwo.class );
		assertEquals( 1, configurationsTwo.size() );
		MockConfigurationTwo configTwo = configurationsTwo.iterator().next();
		assertNotNull( configTwo );
		List<String> loadPathsTwo = configTwo.getLoadPaths();
		assertNotNull( loadPathsTwo );
		assertEquals( 1, loadPathsTwo.size() );
		assertEquals( "app/some-other-subsystem", loadPathsTwo.get(0) );
		
		Set<? extends AbstractSubsystemConfiguration> configurationsAll = unit.getAllMetaData( AbstractSubsystemConfiguration.class );
		
		assertEquals( 2, configurationsAll.size() );
		assertTrue( configurationsAll.contains( configOne ) );
		assertTrue( configurationsAll.contains( configTwo ) );
	}
	
	public static class TriggerAttachmentOne {
		
	}
	
	public static class TriggerAttachmentTwo {
		
	}
	
	public static class MockConfigurationOne extends AbstractSubsystemConfiguration {
	}
	
	public static class MockConfigurationTwo extends AbstractSubsystemConfiguration {
	}
	
	

}
