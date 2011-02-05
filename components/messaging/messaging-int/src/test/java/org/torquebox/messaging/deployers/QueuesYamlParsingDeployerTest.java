package org.torquebox.messaging.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.deployers.TorqueBoxYamlParsingDeployer;
import org.torquebox.messaging.metadata.QueueMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class QueuesYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private TorqueBoxYamlParsingDeployer globalDeployer;
    private QueuesYamlParsingDeployer queuesDeployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.queuesDeployer = new QueuesYamlParsingDeployer();
        addDeployer( this.queuesDeployer );
        this.globalDeployer = new TorqueBoxYamlParsingDeployer();
        addDeployer( this.globalDeployer );
    }

    @Test
    public void testEmptyYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/empty-queues.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "queues.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        assertTrue( allMetaData.isEmpty() );

        undeploy( deploymentName );
    }

    @Test
    public void testJunkYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/junk-queues.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "queues.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        assertTrue( allMetaData.isEmpty() );

        undeploy( deploymentName );
    }

    @Test
    public void testValidYaml() throws Exception {
        File config = new File( System.getProperty( "user.dir" ), "src/test/resources/valid-queues.yml" );
        String deploymentName = addDeployment( config.toURI().toURL(), "queues.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/bar" );
        assertNotNull( queueBar );

        undeploy( deploymentName );
    }

    @Test
    public void testTorqueBoxYml() throws Exception {
        String deploymentName = addDeployment( getClass().getResource( "/valid-torquebox.yml" ), "torquebox.yml" );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );

        undeploy( deploymentName );
    }

    @Test
    public void testTorqueBoxYmlWins() throws Exception {
        JavaArchive jar = createJar( "mystuff.jar" );
        jar.addResource( getClass().getResource( "/valid-queues.yml" ), "/META-INF/queues.yml" );
        jar.addResource( getClass().getResource( "/valid-torquebox.yml" ), "/META-INF/torquebox.yml" );
        String deploymentName = addDeployment( createJarFile( jar ) );

        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends QueueMetaData> allMetaData = unit.getAllMetaData( QueueMetaData.class );

        assertFalse( allMetaData.isEmpty() );

        assertEquals( 2, allMetaData.size() );

        System.err.println( allMetaData );
        QueueMetaData queueFoo = getMetaData( allMetaData, "/queues/tbyaml/foo" );
        assertNotNull( queueFoo );

        QueueMetaData queueBar = getMetaData( allMetaData, "/queues/tbyaml/bar" );
        assertNotNull( queueBar );

        undeploy( deploymentName );
    }

    private QueueMetaData getMetaData(Set<? extends QueueMetaData> allMetaData, String name) {
        for (QueueMetaData each : allMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }

        return null;
    }

}
