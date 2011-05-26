package org.torquebox.core;

import static org.junit.Assert.*;

import java.net.URL;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class TorqueBoxYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
    }

    @Test(expected = DeploymentUnitProcessingException.class)
    public void testBrokenAppRackYml() throws Exception {
        URL torqueboxYml = getClass().getResource( "broken-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        deploy( phaseContext );
    }

    @Test
    public void testEmptyAppRackYml() throws Exception {
        URL torqueboxYml = getClass().getResource( "empty-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );
    }

    @Test
    public void testSectionsTorqueBoxYaml() throws Exception {
        URL torqueboxYml = getClass().getResource( "full-torquebox.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        

        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );

        assertNotNull( metaData.getSection( "application" ) );
        assertNotNull( metaData.getSection( "app" ) );
        assertSame( metaData.getSection("app"), metaData.getSection( "application" ) );
        assertNotNull( metaData.getSection( "web" ) );
        assertNotNull( metaData.getSection( "queues" ) );
        assertNotNull( metaData.getSection( "topics" ) );
        assertNotNull( metaData.getSection( "messaging" ) );
        assertNotNull( metaData.getSection( "services" ) );
        assertNotNull( metaData.getSection( "jobs" ) );
        assertNotNull( metaData.getSection( "ruby" ) );
    }



}
