package org.torquebox.messaging.deployers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.io.File;
import java.util.*;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.torquebox.interp.deployers.DeployerRuby;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;
import org.torquebox.test.ruby.TestRubyFactory;

public class MessagingYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private MessagingYamlParsingDeployer deployer;
    private Ruby ruby;
    private String deploymentName;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new MessagingYamlParsingDeployer();
        addDeployer(this.deployer);
        this.ruby = TestRubyFactory.createRuby();
        this.ruby.evalScriptlet("require 'vfs'");
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
        assertEquals("toast", loadConfig(metaData.getRubyConfig()).get("a"));
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
    public void testMergedMap() throws Exception {
        Set<? extends MessageProcessorMetaData> allMetaData = getMetaData( "src/test/resources/messaging.yml" );
        MessageProcessorMetaData metadata = find( allMetaData, "/hash", "Two" );
        assertEquals( "x > 18", metadata.getMessageSelector() );
        Map config = loadConfig(metadata.getRubyConfig());
        assertEquals( "ex", config.get("x") );
        assertEquals( "why", config.get("y") );
    }

    private Set<? extends MessageProcessorMetaData> getMetaData(String filename) throws Exception {
        File config = new File( System.getProperty( "user.dir" ), filename );
        this.deploymentName = addDeployment(config.toURI().toURL(), "messaging.yml");
        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        unit.addAttachment(DeployerRuby.class, new DeployerRuby(this.ruby));
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

    private Map loadConfig(byte[] bytes) {
        String configStr = RubyString.bytesToString( bytes );
        RubyModule marshal = ruby.getClassFromPath("Marshal");
        return (Map) JavaEmbedUtils.invokeMethod(ruby, marshal, "load", new Object[] { configStr }, Map.class);
    }

}
