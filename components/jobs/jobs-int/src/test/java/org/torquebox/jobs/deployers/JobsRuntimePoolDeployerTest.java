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

package org.torquebox.jobs.deployers;

import static org.junit.Assert.*;

import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class JobsRuntimePoolDeployerTest extends AbstractDeployerTestCase {

    private JobsRuntimePoolDeployer deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new JobsRuntimePoolDeployer();
        addDeployer( this.deployer );
    }

    /** Ensure an existing pool definition is accepted as-is. */
    @Test
    public void testPoolDefinedAlready() throws Exception {
        PoolMetaData jobsPoolMetaData = new PoolMetaData( "jobs" );
        jobsPoolMetaData.setShared();

        String deploymentName = createDeployment( "pool-defined" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        AttachmentUtils.multipleAttach( unit, jobsPoolMetaData, "jobs" );

        processDeployments( true );

        Set<? extends PoolMetaData> allPools = unit.getAllMetaData( PoolMetaData.class );

        assertEquals( 1, allPools.size() );
        assertSame( jobsPoolMetaData, allPools.iterator().next() );

        undeploy( deploymentName );
    }

    /**
     * Ensure an existing pool definition is accepted as-is even if jobs signal
     * requirement.
     */
    @Test
    public void testPoolDefinedAlreadyWithJobs() throws Exception {
        PoolMetaData jobsPoolMetaData = new PoolMetaData( "jobs" );
        jobsPoolMetaData.setShared();

        String deploymentName = createDeployment( "pool-defined" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        AttachmentUtils.multipleAttach( unit, jobsPoolMetaData, "jobs" );

        AttachmentUtils.multipleAttach( unit, new ScheduledJobMetaData(), "job.one" );

        processDeployments( true );

        Set<? extends PoolMetaData> allPools = unit.getAllMetaData( PoolMetaData.class );

        assertEquals( 1, allPools.size() );
        assertSame( jobsPoolMetaData, allPools.iterator().next() );

        undeploy( deploymentName );
    }

    /**
     * Ensure a deployment without a defined pool and without jobs does not
     * define a pool.
     */
    @Test
    public void testNoPoolRequired() throws Exception {

        String deploymentName = createDeployment( "no-pool-required" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        processDeployments( true );

        Set<? extends PoolMetaData> allPools = unit.getAllMetaData( PoolMetaData.class );

        assertTrue( allPools.isEmpty() );

        undeploy( deploymentName );
    }

    /**
     * Ensure a deployment without a defined pool and with jobs does define a
     * pool.
     */
    @Test
    public void testPoolRequired() throws Exception {

        String deploymentName = createDeployment( "pool-required" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        ScheduledJobMetaData rubyJobMetaData = new ScheduledJobMetaData();
        AttachmentUtils.multipleAttach( unit, rubyJobMetaData, "job.one" );

        processDeployments( true );

        Set<? extends PoolMetaData> allPools = unit.getAllMetaData( PoolMetaData.class );

        assertFalse( allPools.isEmpty() );

        PoolMetaData jobsPoolMetaData = null;

        for (PoolMetaData each : allPools) {
            if (each.getName().equals( "jobs" )) {
                jobsPoolMetaData = each;
                break;
            }
        }

        assertNotNull( jobsPoolMetaData );

        undeploy( deploymentName );
    }

}
