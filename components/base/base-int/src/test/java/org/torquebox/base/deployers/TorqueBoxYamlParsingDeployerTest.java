package org.torquebox.base.deployers;

import static org.junit.Assert.*;

import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class TorqueBoxYamlParsingDeployerTest extends AbstractDeployerTestCase {
    
    @Before
    public void setUp() throws Throwable {
        addDeployer( new TorqueBoxYamlParsingDeployer() );
    }
    
    @Test(expected=DeploymentException.class)
    public void testBrokenAppRackYml() throws Exception {
        URL appRackYml = getClass().getResource("/broken-torquebox.yml");
        
        addDeployment(appRackYml, "torquebox.yml");
        processDeployments(true);
    }
    
    @Test
    public void testEmptyAppRackYml() throws Exception {
        URL appRackYml = getClass().getResource("/empty-torquebox.yml");
        
        String deploymentName = addDeployment(appRackYml, "torquebox.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        assertNotNull( unit );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.class );
        assertNotNull( metaData );
    }
    
    @Test
    public void testSectionsTorqueBoxYaml() throws Exception {
        URL appRackYml = getClass().getResource("/sections-torquebox.yml");
        
        String deploymentName = addDeployment(appRackYml, "torquebox.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        assertNotNull( unit );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.class );
        assertNotNull( metaData );
        
        assertNotNull( metaData.getSection( "app" ) );
        assertNotNull( metaData.getSection( "web" ) );
        assertNotNull( metaData.getSection( "queues" ) );
        assertNotNull( metaData.getSection( "topics" ) );
        assertNotNull( metaData.getSection( "messaging" ) );
        assertNotNull( metaData.getSection( "services" ) );
        assertNotNull( metaData.getSection( "jobs" ) );
    }

}
