package org.torquebox.messaging.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.messaging.MessageProcessorMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class MessagingRuntimePoolProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        appendDeployer( new MessagingRuntimePoolProcessor() );
    }

    /**
     * Ensure a deployment without a defined pool and with message processors
     * does define a pool.
     */
    @Test
    public void testPoolRequired() throws Exception {
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        unit.addToAttachmentList( MessageProcessorMetaData.ATTACHMENTS_KEY, new MessageProcessorMetaData() );

        deploy( phaseContext );

        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );

        PoolMetaData messagingPoolMetaData = allPools.get( 0 );
        assertEquals( "messaging", messagingPoolMetaData.getName() );
        assertTrue( messagingPoolMetaData.isShared() );
        assertTrue( messagingPoolMetaData.isDeferUntilRequested() );
        assertFalse( messagingPoolMetaData.isStartAsynchronously() );
    }

}
