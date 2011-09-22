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

package org.torquebox.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.Value;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.jobs.as.JobsServices;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;
import org.torquebox.test.as.MockServiceBuilder;

public class ScheduledJobDeployerTest extends AbstractDeploymentProcessorTestCase {
    
    @Before
    public void setUp() {
        appendDeployer( new ScheduledJobDeployer() );
    }
    

    /** Ensure that non-matching deployments are un-affected. */
    @Test
    public void testEmptyDeployment() throws Exception {
        MockDeploymentPhaseContext phaseContext = createPhaseContext();
        
        deploy(  phaseContext );
        
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
        rubyAppMetaData.attachTo( unit );
        
        deploy( phaseContext );

        ServiceName jobServiceName = JobsServices.scheduledJob( unit, "job.one" );
        
        MockServiceBuilder<?> builder = phaseContext.getMockServiceTarget().getMockServiceBuilder( jobServiceName );
        
        assertNotNull( builder );
        
        Value<?> jobService = builder.getValue();
        assertNotNull( jobService );
        
        ScheduledJob job = (ScheduledJob) jobService.getValue();
        assertNotNull( job );
        assertEquals( "job.one", job.getName() );
        assertEquals( "22 * * * * ?", job.getCronExpression() );
        assertEquals( "TestJob", job.getRubyClassName() );
    }
}
