/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.jobs.processors;

import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.projectodd.polyglot.test.as.MockServiceBuilder;
import org.projectodd.polyglot.test.as.MockServiceRegistry;
import org.torquebox.core.app.RubyAppMetaData;
import org.torquebox.jobs.JobScheduler;
import org.torquebox.jobs.JobSchedulerMetaData;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JobSchedulerInstallerTest extends AbstractDeploymentProcessorTestCase {

    class ClusteredMockServiceRegistry extends MockServiceRegistry {
        ClusteredMockServiceRegistry() {
            this.registry.put(ChannelFactoryService.getServiceName(null), Mockito.mock(ServiceController.class));
        }
    }

    private void init(MockServiceRegistry serviceRegistry) throws Exception {
        this.phaseContext = createPhaseContext(serviceRegistry, "test-unit");
        this.unit = this.phaseContext.getMockDeploymentUnit();

        this.unit.putAttachment( JobSchedulerMetaData.ATTACHMENT_KEY, new JobSchedulerMetaData());
        this.unit.putAttachment( RubyAppMetaData.ATTACHMENT_KEY, new RubyAppMetaData("test-app"));
    }

    @Before
    public void setUp() throws Exception {
        appendDeployer( new JobSchedulerInstaller() );

        init(new MockServiceRegistry());
    }
    
    /** Ensure that given no jobs, a scheduler is still deployed (for runtime scheduling of jobs). */
    @Test
    public void testNoJobsButSchedulerShouldBeDeployedEitherCase() throws Exception {
        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.scheduler(unit, false);
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        assertEquals( 1, phaseContext.getMockServiceTarget().getMockServiceBuilders().size() );
    }

    /** Ensure that in a clustered environment two schedulers are deployed */
    @Test
    public void testClusteredSchedulerDeployment() throws Exception {
        // Prepare a special 'clustered' phase context
        init(new ClusteredMockServiceRegistry());

        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.scheduler(unit, false);
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        assertEquals( 2, phaseContext.getMockServiceTarget().getMockServiceBuilders().size() );
    }

    /** Ensure that given at least one job, a scheduler is deployed. */
    @Test
    public void testSchedulerDeployment() throws Exception {
        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMeta );
        
        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.scheduler(unit, false);
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
    @Test
    public void testSingletonSchedulerDeployment() throws Throwable {
        // Prepare a special 'clustered' phase context
        init(new ClusteredMockServiceRegistry());

        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        jobMeta.setSingleton( true );
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMeta );
        
        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.scheduler(unit, true);
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        
        Value<?> value = builder.getValue();
        assertNotNull( value );
        
        JobScheduler scheduler = (JobScheduler) value.getValue();
        assertNotNull( scheduler );
    
    }
    
    /** Ensure that a thread count is passed through. */
    @Test
    public void testWithAThreadCount() throws Exception {
        this.unit.getAttachment( JobSchedulerMetaData.ATTACHMENT_KEY ).setThreadCount( 55 );
        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        unit.addToAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY, jobMeta );
        
        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.scheduler(unit, false);
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        
        Value<?> value = builder.getValue();
        assertNotNull( value );
        
        JobScheduler scheduler = (JobScheduler) value.getValue();
        assertNotNull( scheduler );
        
        assertEquals( 55, scheduler.getThreadCount() );
    }
    private MockDeploymentPhaseContext phaseContext;
    private MockDeploymentUnit unit;

}
