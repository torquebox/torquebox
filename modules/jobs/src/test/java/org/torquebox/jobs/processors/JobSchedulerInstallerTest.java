/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.polyglot.test.as.MockDeploymentPhaseContext;
import org.projectodd.polyglot.test.as.MockDeploymentUnit;
import org.projectodd.polyglot.test.as.MockServiceBuilder;
import org.torquebox.jobs.JobScheduler;
import org.torquebox.jobs.ScheduledJobMetaData;
import org.torquebox.jobs.as.JobsServices;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;

public class JobSchedulerInstallerTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() {
        appendDeployer( new JobSchedulerInstaller( false ) );
    }
    
    /** Ensure that given no jobs, a scheduler is not deployed. */
    @Test
    public void testNoJobsNoScheduler() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        
        deploy( phaseContext );

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
        
        deploy( phaseContext );

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
        
        deploy( phaseContext );

        ServiceName schedulerServiceName = JobsServices.jobScheduler( unit, true );
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( schedulerServiceName );
        assertNotNull( builder );
        
        Value<?> value = builder.getValue();
        assertNotNull( value );
        
        JobScheduler scheduler = (JobScheduler) value.getValue();
        assertNotNull( scheduler );
    
    }

}
