package org.torquebox.core.pool;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

import static org.junit.Assert.*;

public class PoolingYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new PoolingYamlParsingProcessor() );
    }

    @Test
    public void testEmptyPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );

        assertTrue( unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY ).isEmpty() );

    }

    @Test(expected=DeploymentUnitProcessingException.class)
    public void testJunkPoolingYml() throws Exception {

        URL poolingYml = getClass().getResource( "junk-pooling.yml" );
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
    }

    @Test
    public void testMinMaxPoolingYml() throws Exception {
        URL poolingYml = getClass().getResource( "min-max-pooling.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", poolingYml );
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        deploy( phaseContext );
        
        List<PoolMetaData> pools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );

        assertFalse( pools.isEmpty() );
        assertEquals( 2, pools.size() );
        
        PoolMetaData poolOne = pools.get( 0 );
        assertNotNull( poolOne );

        assertEquals( "pool_one", poolOne.getName() );
        assertEquals( 1, poolOne.getMinimumSize() );
        assertEquals( 1, poolOne.getMaximumSize() );

        PoolMetaData poolTwo = pools.get( 1 );

        assertEquals( "pool_two", poolTwo.getName() );
        assertEquals( 10, poolTwo.getMinimumSize() );
        assertEquals( 200, poolTwo.getMaximumSize() );
    }

}
