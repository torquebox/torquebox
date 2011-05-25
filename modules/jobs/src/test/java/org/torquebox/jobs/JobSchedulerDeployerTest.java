package org.torquebox.jobs;

import static org.junit.Assert.*;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.jobs.as.JobsServices;

public class JobSchedulerDeployerTest extends AbstractDeploymentProcessorTestCase {
    
    private JobSchedulerDeployer deployer;

    @Before
    public void setUp() {
        this.deployer = new JobSchedulerDeployer();
    }
    
    /** Ensure that given no jobs, a scheduler is not deployed. */
    @Test
    public void testNoJobsNoScheduler() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        this.deployer.deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.jobScheduler( unit, false );
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNull( builder );
        assertEquals( 0, phaseContext.getMockServiceTarget().getMockServiceBuilders().size() );
    }

    /** Ensure that given at least one job, a scheduler is deployed. */
    @Test
    public void testSchedulerDeployment() throws Exception {

        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMeta );
        
        this.deployer.deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.jobScheduler( unit, false );
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        
        Value<?> value = builder.getValue();
        assertNotNull( value );
        
        JobScheduler scheduler = (JobScheduler) value.getValue();
        assertNotNull( scheduler );
    }
    
    /** Ensure that we create a singleton deployer in a clustered environment 
     * @throws Throwable 
     * */
    @Ignore
    @Test
    public void testSingletonSchedulerDeployment() throws Throwable {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        jobMeta.setSingleton( true );
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMeta );
        
        this.deployer.deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.jobScheduler( unit, true );
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        
        Value<?> value = builder.getValue();
        assertNotNull( value );
        
        JobScheduler scheduler = (JobScheduler) value.getValue();
        assertNotNull( scheduler );
    
    }

}
