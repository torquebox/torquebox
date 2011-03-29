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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.core.RubyScheduler;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubySchedulerDeployerTest extends AbstractDeployerTestCase {

    private RubySchedulerDeployer deployer;

    @Before
    public void setUp() throws Throwable {
        this.deployer = new RubySchedulerDeployer();
        this.deployer.setRubyRuntimePoolName( "runtime-pool" );
        addDeployer( this.deployer );
    }

    /** Ensure that given no jobs, a scheduler is not deployed. */
    @Test
    public void testNoJobsNoScheduler() throws Exception {

        String deploymentName = createDeployment( "no-jobs" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        processDeployments( true );

        String schedulerBeanName = AttachmentUtils.beanName( unit, RubyScheduler.class );
        BeanMetaData schedulerMetaData = getBeanMetaData( unit, schedulerBeanName );
        assertNull( schedulerMetaData );

        undeploy( deploymentName );
    }

    /** Ensure that given at least one job, a scheduler is deployed. */
    @Test
    public void testSchedulerDeployment() throws Exception {

    	JavaArchive archive = createJar( "scheduler-support" );
        archive.addResource( getClass().getResource( "interp-jboss-beans.xml" ), "interp-jboss-beans.xml" );
        File archiveFile = createJarFile( archive );

        String supportDeploymentName = addDeployment( archiveFile );
        processDeployments( true );

        String deploymentName = createDeployment( "with-jobs" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        ScheduledJobMetaData jobMeta = new ScheduledJobMetaData();
        AttachmentUtils.multipleAttach( unit, jobMeta, "job.one" );

        processDeployments( true );

        String schedulerBeanName = AttachmentUtils.beanName( unit, RubyScheduler.class );
        BeanMetaData schedulerMetaData = getBeanMetaData( unit, schedulerBeanName );
        assertNotNull( schedulerMetaData );

        RubyScheduler scheduler = (RubyScheduler) getBean( schedulerBeanName );
        assertNotNull( scheduler );


        undeploy( deploymentName );
        undeploy( supportDeploymentName );

    }

}
