package org.torquebox.messaging.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class MessagingYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private MessagingYamlParsingDeployer deployer;
    private String deploymentName;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new MessagingYamlParsingDeployer();
        addDeployer(this.deployer);
    }

    @After
    public void tearDownDeployer() throws Throwable {
        undeploy(this.deploymentName);
    }

    @Test
    public void testEmptyMessagingConfig() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/empty-messaging.yml" );
        assertTrue(allMetaData.isEmpty());
    }

    @Test(expected = DeploymentException.class)
    public void testJunkMessagingConfig() throws Exception {
        getMetaData( "src/test/resources/junk-messaging.yml" );
    }

    @Test
    public void testSingleMessagingConfig() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/single-messaging.yml" );
        assertEquals(1, allMetaData.size());

        MessageProcessorMetaData metaData = allMetaData.iterator().next();
        assertNotNull(metaData);
        assertEquals("MyClass", metaData.getRubyClassName());
        assertEquals("/topics/foo", metaData.getDestinationName());
        assertEquals("myfilter", metaData.getMessageSelector());
        assertEquals("toast", metaData.getRubyConfig().get("a"));
        assertEquals(new Integer(2), metaData.getConcurrency());
    }

    @Test
    public void testMappingsFromAllConfigStyles() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        assertEquals( 7, allMetaData.size() );
        assertEquals( 1, filter( allMetaData, "/simple" ).size() );
        assertEquals( 3, filter( allMetaData, "/array" ).size() );
        assertEquals( 3, filter( allMetaData, "/hash" ).size() );
        assertNotNull( find( allMetaData, "/simple", "Simple" ) );
        assertNotNull( find( allMetaData, "/array", "One" ) );
        assertNotNull( find( allMetaData, "/array", "Two" ) );
        assertNotNull( find( allMetaData, "/array", "Three" ) );
        assertNotNull( find( allMetaData, "/hash", "A" ) );
        assertNotNull( find( allMetaData, "/hash", "B" ) );
        assertNotNull( find( allMetaData, "/hash", "Two" ) );
    }

    @Test
    public void testConfigOptionsForArray() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/array", "Two" );
        assertEquals( "x > 18", metadata.getMessageSelector() );
        Map config = metadata.getRubyConfig();
        assertEquals( "ex", config.get("x") );
        assertEquals( "why", config.get("y") );
        assertTrue( isUnconfigured( find( allMetaData, "/array", "One" ) ) );
        assertTrue( isUnconfigured( find( allMetaData, "/array", "Three" ) ) );
    }

    @Test
    public void testConfigOptionsForHash() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "B" );
        assertEquals( "y < 18", metadata.getMessageSelector() );
        Map config = metadata.getRubyConfig();
        assertEquals( "ache", config.get("h") );
        assertEquals( "eye", config.get("i") );
        assertEquals( new Integer(3), metadata.getConcurrency() );
        assertTrue( isUnconfigured( find( allMetaData, "/hash", "A" ) ) );
    }

    @Test
    public void testMergedMap() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "Two" );
        assertEquals( "x > 18", metadata.getMessageSelector() );
        Map config = metadata.getRubyConfig();
        assertEquals( "ex", config.get("x") );
        assertEquals( "why", config.get("y") );
    }

    @Test
    public void testDefaultConcurrency() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "A" );
        assertEquals( new Integer(1), metadata.getConcurrency() );
    }

    private Set<? extends MessageProcessorMetaData> getMetaData(String filename) throws Exception {
        File config = new File( System.getProperty( "user.dir" ), filename );
        this.deploymentName = addDeployment(config.toURI().toURL(), "messaging.yml");
        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        //unit.addAttachment(DeployerRuby.class, new DeployerRuby(this.ruby));
        processDeployments(true);
        return unit.getAllMetaData(MessageProcessorMetaData.class);
    }

    private List<MessageProcessorMetaData> filter(Set<? extends MessageProcessorMetaData> metadata, String destination) {
        List<MessageProcessorMetaData> results = new ArrayList<MessageProcessorMetaData>();
        for (MessageProcessorMetaData md: metadata) {
            if (destination.equals(md.getDestinationName())) {
                results.add( md );
            }
        }
        return results;
    }

    private MessageProcessorMetaData find(Set<? extends MessageProcessorMetaData> metadata, String destination, String handler) {
        for (MessageProcessorMetaData md: metadata) {
            if (destination.equals(md.getDestinationName()) && handler.equals(md.getRubyClassName())) {
                return md;
            }
        }
        return null;
    }

    private boolean isUnconfigured(MessageProcessorMetaData metadata) {
        return null == metadata.getMessageSelector() && metadata.getRubyConfig().isEmpty();
    }
}
