package org.torquebox.core.app;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class EnvironmentYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new EnvironmentYamlParsingProcessor() );
    }

    @Test
    public void testBooleanEnvironmentYml() throws Exception {
        URL environmentYml = getClass().getResource( "environment.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", environmentYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        deploy( phaseContext );

        RubyApplicationMetaData appMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        assertNotNull( appMetaData );

        Map<String, String> environmentVariables = appMetaData.getEnvironmentVariables();
        assertNotNull( environmentVariables );

        String booleanVariable = environmentVariables.get( "A_BOOLEAN_VALUE" );
        assertEquals( "true", booleanVariable );
    }

}
