package org.torquebox.messaging;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class QueuesYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new QueuesYamlParsingProcessor() );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        URL queuesYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "queues.yml", queuesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testJunkYaml() throws Exception {
        URL queuesYml = getClass().getResource( "junk-queues.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "queues.yml", queuesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testValidYaml() throws Exception {
        URL queuesYml = getClass().getResource( "valid-queues.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "queues.yml", queuesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 2, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/bar" );
        assertNotNull( queueBar );
    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        URL queuesYml = getClass().getResource( "valid-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", queuesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 3, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );
        
        QueueMetaData queueFooBar = getMetaData( allMetaData, "/queues/tbyaml/foobar" );
        assertNotNull( queueFooBar );
    }

    @Ignore @Test
    public void testTorqueBoxYmlWins() throws Exception {
        // TODO fix multi-asset deployer tests.
        
//        JavaArchive jar = createJar( "mystuff.jar" );
//        jar.addResource( getClass().getResource( "/valid-queues.yml" ), "/META-INF/queues.yml" );
//        jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
//        String deploymentName = addDeployment( createJarFile( jar ) );
//
//        processDeployments( true );
//
//        DeploymentUnit unit = getDeploymentUnit( deploymentName );
//        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );
//
//        assertFalse( allMetaData.isEmpty() );
//
//        assertEquals( 3, allMetaData.size() );
//
//        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
//        assertNotNull( queueFoo );
//
//        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
//        assertNotNull( queueBar );
//
//        undeploy( deploymentName );
    }

    @Test
    public void testDestinationDurability() throws Exception {
        
        URL queuesYml = getClass().getResource( "valid-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", queuesYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<QueueMetaData> allMetaData = unit.getAttachmentList( QueueMetaData.ATTACHMENTS_KEY );

        assertEquals( 3, allMetaData.size() );

        // /queues/tbyaml/foo has no durability flag set, we should default to
        // durable
        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );
        assertTrue( queueFoo.isDurable() );

        // /queues/tbyaml/bar has durability set to true, we should reflect that
        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );
        assertFalse( queueBar.isDurable() );
        
        // /queues/tbyaml/bar has durability set to false, we should reflect that
        QueueMetaData queueFooBar = getMetaData( allMetaData, "/queues/tbyaml/foobar" );
        assertNotNull( queueFooBar );
        assertTrue( queueFooBar.isDurable() );
        
    }

    private QueueMetaData getMetaData(List<QueueMetaData> allMetaData, String name) {
        for (QueueMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
