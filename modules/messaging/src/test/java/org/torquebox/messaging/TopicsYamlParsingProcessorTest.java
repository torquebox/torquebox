package org.torquebox.messaging;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class TopicsYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor()  );
        appendDeployer( new TopicsYamlParsingProcessor()  );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        URL topicsYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testJunkYaml() throws Exception {
        URL topicsYml = getClass().getResource( "junk-topics.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );
        assertTrue( allMetaData.isEmpty() );
    }

    @Test
    public void testValidYaml() throws Exception {
        URL topicsYml = getClass().getResource( "valid-topics.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "topics.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/bar" );
        assertNotNull( topicBar );

    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        URL topicsYml = getClass().getResource( "valid-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", topicsYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );
        
        List<TopicMetaData> allMetaData = unit.getAttachmentList( TopicMetaData.ATTACHMENTS_KEY );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
        assertNotNull( topicFoo );

        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
        assertNotNull( topicBar );
    }

    @Ignore @Test
    public void testTorqueBoxYmlWins() throws Exception {
//        JavaArchive jar = createJar( "mystuff.jar" );
//        jar.addResource( getClass().getResource( "/valid-topics.yml" ), "/META-INF/topics.yml" );
//        jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
//        String deploymentName = addDeployment( createJarFile( jar ) );
//
//        processDeployments( true );
//
//        DeploymentUnit unit = getDeploymentUnit( deploymentName );
//        Set<? extends TopicMetaData> allMetaData = unit.getAllMetaData( TopicMetaData.class );
//
//        assertFalse( allMetaData.isEmpty() );
//
//        assertEquals( 2, allMetaData.size() );
//
//        TopicMetaData topicFoo = getMetaData( allMetaData, "/topics/tbyaml/foo" );
//        assertNotNull( topicFoo );
//
//        TopicMetaData topicBar = getMetaData( allMetaData, "/topics/tbyaml/bar" );
//        assertNotNull( topicBar );
//
//        undeploy( deploymentName );
    }

    private TopicMetaData getMetaData(List<TopicMetaData> allMetaData, String name) {
        for (TopicMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
