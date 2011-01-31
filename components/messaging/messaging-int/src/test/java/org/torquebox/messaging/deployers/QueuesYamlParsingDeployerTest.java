package org.torquebox.messaging.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class QueuesYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private QueuesYamlParsingDeployer deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new QueuesYamlParsingDeployer();
        addDeployer(this.deployer);
    }

    @Test
    public void testEmptyYaml() throws Exception {
        File config = new File(System.getProperty("user.dir"), "src/test/resources/empty-queues.yml");
        String deploymentName = addDeployment(config.toURI().toURL(), "queues.yml");

        processDeployments(true);

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData(QueueMetaData.class);

        assertTrue(allMetaData.isEmpty());

        undeploy(deploymentName);
    }

    @Test
    public void testJunkYaml() throws Exception {
        File config = new File(System.getProperty("user.dir"), "src/test/resources/junk-queues.yml");
        String deploymentName = addDeployment(config.toURI().toURL(), "queues.yml");

        processDeployments(true);

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData(QueueMetaData.class);

        assertTrue(allMetaData.isEmpty());

        undeploy(deploymentName);
    }

    @Test
    public void testValidYaml() throws Exception {
        File config = new File(System.getProperty("user.dir"), "src/test/resources/valid-queues.yml");
        String deploymentName = addDeployment(config.toURI().toURL(), "queues.yml");

        processDeployments(true);

        DeploymentUnit unit = getDeploymentUnit(deploymentName);
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData(QueueMetaData.class);

        assertFalse(allMetaData.isEmpty());

        assertEquals(2, allMetaData.size());

        QueueMetaData queueFoo = getMetaData(allMetaData, "/queues/foo");
        assertNotNull(queueFoo);

        QueueMetaData queueBar = getMetaData(allMetaData, "/queues/bar");
        assertNotNull(queueBar);

        undeploy(deploymentName);
    }

    private QueueMetaData getMetaData(Set<? extends QueueMetaData> allMetaData, String name) {
        for (QueueMetaData each : allMetaData) {
            if (each.getName().equals(name)) {
                return each;
            }
        }

        return null;
    }

}
