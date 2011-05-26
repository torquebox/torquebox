package org.torquebox.core.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.TorqueBoxMetaData;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class ApplicationYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new ApplicationYamlParsingProcessor() );
        appendDeployer( new RubyApplicationDefaultsProcessor() );
    }

    @Test
    public void testSimpleTorqueBoxYml() throws Exception {
        URL torqueboxYml = getClass().getResource( "simple-torquebox.yml" );

        
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", torqueboxYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        deploy( phaseContext );
        
        TorqueBoxMetaData metaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );
        assertNotNull( metaData );

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );
        assertNotNull( rubyAppMetaData );
        
        assertEquals( RubyApplicationMetaData.DEFAULT_ENVIRONMENT_NAME, rubyAppMetaData.getEnvironmentName() );
        
        assertEquals( "torquebox", rubyAppMetaData.getApplicationName() );
    }

}
