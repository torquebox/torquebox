package org.torquebox.core.app;

import java.net.URL;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;

public class AppKnobYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    

    @Before
    public void setUp() throws Throwable {
        appendDeployer( new AppKnobYamlParsingProcessor() );
    }

    @Test(expected=DeploymentUnitProcessingException.class)
    public void testInvalidRootKnob() throws Exception {
        
        URL appKnobYml = getClass().getResource( "invalid-root-knob.yml" );
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "invalid-root-knob.yml", appKnobYml );
        deploy( phaseContext );
    }

}
