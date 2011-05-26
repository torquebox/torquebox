package org.torquebox.web.rack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class WebYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
	
	@Before
	public void setUp() {
		appendDeployer( new TorqueBoxYamlParsingProcessor() );
		appendDeployer( new WebYamlParsingProcessor() );
	}
	
    @Test()
    public void testEmptyWebYml() throws Exception {
        URL webYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", webYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        
        assertNull( unit.getAttachment(RackApplicationMetaData.ATTACHMENT_KEY ));
    }

    @Test
    public void testValidWebYml() throws Exception {
        URL webYml = getClass().getResource( "valid-web.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", webYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );

        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertNull( rackMetaData.getStaticPathPrefix() );
    }

    @Test
    public void testValidWebYmlCustomStaticPathPrefix() throws Exception {
        URL webYml = getClass().getResource( "static-path-web.yml" );
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", webYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );

        RackApplicationMetaData rackMetaData = unit.getAttachment( RackApplicationMetaData.ATTACHMENT_KEY );

        assertNotNull( rackMetaData );

        assertEquals( "/tacos", rackMetaData.getContextPath() );
        assertEquals( 1, rackMetaData.getHosts().size() );
        assertEquals( "foobar.com", rackMetaData.getHosts().get( 0 ) );
        assertEquals( "/elsewhere", rackMetaData.getStaticPathPrefix() );
    }

}
