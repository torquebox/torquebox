package org.torquebox.messaging.deployers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Set;
import java.io.File;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.deployers.DeployerRuby;
import org.torquebox.messaging.metadata.MessageProcessorMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;
import org.torquebox.test.ruby.TestRubyFactory;

public class MessagingYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private MessagingYamlParsingDeployer deployer;
    private Ruby ruby;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new MessagingYamlParsingDeployer();
        addDeployer(this.deployer);
        this.ruby = TestRubyFactory.createRuby();
        this.ruby.evalScriptlet("require 'vfs'");
    }

    @Test
    public void testEmptyMessagingConfig() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/empty-messaging.yml" );
        String deploymentName = addDeployment(config.toURI().toURL(), "messaging.yml");

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        unit.addAttachment(DeployerRuby.class, new DeployerRuby(this.ruby));

        processDeployments(true);

        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData(MessageProcessorMetaData.class);

        assertTrue(allMetaData.isEmpty());
        undeploy(deploymentName);
    }

    @Test(expected = DeploymentException.class)
    public void testJunkMessagingConfig() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/junk-messaging.yml" );
        String deploymentName = addDeployment(config.toURI().toURL(), "messaging.yml");

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        unit.addAttachment(DeployerRuby.class, new DeployerRuby(this.ruby));

        processDeployments(true);
    }

    @Test
    public void testSingleMessagingConfig() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/single-messaging.yml" );
        String deploymentName = addDeployment(config.toURI().toURL(), "messaging.yml");

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        unit.addAttachment(DeployerRuby.class, new DeployerRuby(this.ruby));

        processDeployments(true);

        Set<? extends MessageProcessorMetaData> allMetaData = unit.getAllMetaData(MessageProcessorMetaData.class);

        assertEquals(1, allMetaData.size());

        MessageProcessorMetaData metaData = allMetaData.iterator().next();

        assertNotNull(metaData);

        assertEquals("MyClass", metaData.getRubyClassName());
        assertEquals("/topics/foo", metaData.getDestinationName());
        assertEquals("myfilter", metaData.getMessageSelector());
        byte[] rubyConfig = metaData.getRubyConfig();
        assertNotNull(rubyConfig);

        undeploy(deploymentName);
    }

}
