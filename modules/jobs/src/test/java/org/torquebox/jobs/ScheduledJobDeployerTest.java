package org.torquebox.jobs;

import static org.junit.Assert.*;

import java.util.Collection;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.jobs.as.JobsServices;

public class ScheduledJobDeployerTest extends AbstractDeploymentProcessorTestCase {
    
    private ScheduledJobDeployer deployer;

    @Before
    public void setUp() {
        this.deployer = new ScheduledJobDeployer();
    }
    

    /** Ensure that non-matching deployments are un-affected. */
    @Test
    public void testEmptyDeployment() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        
        this.deployer.deploy(  phaseContext );
        
        Collection<MockServiceBuilder<?>> allBuilders = phaseContext.getMockServiceTarget().getMockServiceBuilders();
        assertNotNull( allBuilders );
        assertTrue( allBuilders.isEmpty() );

    }
    

    @Test
    public void testSimpleDeployment() throws Exception {
        
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();
        jobMetaData.setName( "job.one" );
        jobMetaData.setDescription( "test job" );
        jobMetaData.setRubyClassName( "TestJob" );
        jobMetaData.setCronExpression( "22 * * * * ?" );

        jobMetaData.setRubySchedulerName( "scheduler" );
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMetaData );
        
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData( "test-app");
        unit.putAttachment( RubyApplicationMetaData.ATTACHMENT_KEY, rubyAppMetaData );
        
        this.deployer.deploy( phaseContext );

        ServiceName jobServiceName = JobsServices.scheduledJob( unit, "job.one" );
        
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( jobServiceName );
        
        assertNotNull( builder );
        
        Value<?> jobService = builder.getValue();
        assertNotNull( jobService );
        
        ScheduledJob job = (ScheduledJob) jobService.getValue();
        assertNotNull( job );
        
        assertNotNull( job );
        assertEquals( "job.one", job.getName() );
        assertEquals( "22 * * * * ?", job.getCronExpression() );
        assertEquals( "TestJob", job.getRubyClassName() );
    }
}
