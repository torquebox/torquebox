package org.torquebox.jobs;

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.PoolMetaData;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class JobsRuntimePoolProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUpDeployer() throws Throwable {
        addDeployer( new JobsRuntimePoolProcessor() );
    }

    /** Ensure an existing pool definition is accepted as-is. */
    @Test
    public void testPoolDefinedAlready() throws Exception {
        
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        PoolMetaData jobsPoolMetaData = new PoolMetaData( "jobs" );
        jobsPoolMetaData.setShared();
        
        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, jobsPoolMetaData );
        deploy( phaseContext );
        
        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );
        
        assertSame( jobsPoolMetaData, allPools.get( 0 ));
    }

    /**
     * Ensure an existing pool definition is accepted as-is even if jobs signal
     * requirement.
     */
    @Test
    public void testPoolDefinedAlreadyWithJobs() throws Exception {
        
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        PoolMetaData jobsPoolMetaData = new PoolMetaData( "jobs" );
        jobsPoolMetaData.setShared();
        
        unit.addToAttachmentList( PoolMetaData.ATTACHMENTS_KEY, jobsPoolMetaData );
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, new ScheduledJobMetaData() );
        
        deploy( phaseContext );
        
        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );
        
        assertSame( jobsPoolMetaData, allPools.get( 0 )); 
    }

    /**
     * Ensure a deployment without a defined pool and without jobs does not
     * define a pool.
     */
    @Test
    public void testNoPoolRequired() throws Exception {
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        deploy( phaseContext );
        
        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 0, allPools.size() );
    }

    /**
     * Ensure a deployment without a defined pool and with jobs does define a
     * pool.
     */
    @Test
    public void testPoolRequired() throws Exception {
        DeploymentPhaseContext phaseContext = createPhaseContext();
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        
        
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, new ScheduledJobMetaData() );
        
        deploy( phaseContext );
        
        List<PoolMetaData> allPools = unit.getAttachmentList( PoolMetaData.ATTACHMENTS_KEY );
        assertEquals( 1, allPools.size() );
        
        PoolMetaData jobPoolMetaData = allPools.get( 0 );
        
        assertEquals( "jobs", jobPoolMetaData.getName() );
        assertTrue( jobPoolMetaData.isShared() );
    }

}
