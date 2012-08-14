package org.torquebox.services.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.services.ServiceMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class ServiceRuntimePoolProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new ServiceRuntimePoolProcessor() );
    }

    /**
     * Ensure a deployment without a defined pool and with service
     * does define a pool.
     */
    @Test
    public void testPoolRequired() throws Exception {
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        unit.addToAttachmentList( ServiceMetaData.ATTACHMENTS_KEY, new ServiceMetaData() );

        deploy( phaseContext );

        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );

        PoolMetaData servicePoolMetaData = allPools.get( 0 );
        assertEquals( "services", servicePoolMetaData.getName() );
        assertTrue( servicePoolMetaData.isShared() );
        assertTrue( servicePoolMetaData.isDeferUntilRequested() );
    }

}
