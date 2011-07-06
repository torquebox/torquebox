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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.jobs.core.ScheduledJob;
import org.torquebox.jobs.core.SchedulerProxy;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RubyJobDeployerTest extends AbstractDeployerTestCase {

    private RubyJobDeployer deployer;

    @Before
    public void setUp() throws Throwable {
        this.deployer = new RubyJobDeployer();
        addDeployer( this.deployer );
    }

    /** Ensure that non-matching deployments are un-affected. */
    @Test
    public void testEmptyDeployment() throws Exception {
        String deploymentName = createDeployment( "not-relevant" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        Set<? extends BeanMetaData> allBeanMetaData = unit.getAllMetaData( BeanMetaData.class );

        assertNotNull( allBeanMetaData );
        assertTrue( allBeanMetaData.isEmpty() );
    }

    @Test
    public void testSimpleDeployment() throws Exception {
        JavaArchive archive = createJar( "scheduler-dependencies" );
        archive.addResource( getClass().getResource( "interp-jboss-beans.xml" ), "interp-jboss-beans.xml" );
        archive.addResource( getClass().getResource( "scheduler-jboss-beans.xml" ), "scheduler-jboss-beans.xml" );
        File archiveFile = createJarFile( archive );

        String supportDeploymentName = addDeployment( archiveFile );
        processDeployments( true );

        SchedulerProxy schedulerProxy = new SchedulerProxy();
        
        ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();
        jobMetaData.setName( "job.one" );
        jobMetaData.setDescription( "test job" );
        jobMetaData.setRubyClassName( "TestJob" );
        jobMetaData.setCronExpression( "22 * * * * ?" );
        jobMetaData.setSchedulerProxy( schedulerProxy );
        jobMetaData.setRubySchedulerName( "scheduler" );

        String deploymentName = createDeployment( "simple" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        AttachmentUtils.multipleAttach( unit, jobMetaData, "job.one" );
        
        RubyApplicationMetaData rubyAppMetaData = new RubyApplicationMetaData();
        rubyAppMetaData.setApplicationName(  "test-app" );
        unit.addAttachment( RubyApplicationMetaData.class, rubyAppMetaData );

        processDeployments( true );

        String jobBeanName = AttachmentUtils.beanName( unit, ScheduledJob.class, "job.one" );
        BeanMetaData jobBeanMetaData = getBeanMetaData( unit, jobBeanName );
        assertNotNull( jobBeanMetaData );

        ScheduledJob jobBean = (ScheduledJob) getBean( jobBeanName );
        assertNotNull( jobBean );
        assertEquals( "job.one", jobBean.getName() );
        assertEquals( "22 * * * * ?", jobBean.getCronExpression() );
        assertEquals( "TestJob", jobBean.getRubyClassName() );
        assertEquals( schedulerProxy, jobBean.getSchedulerProxy() );
        
        undeploy( deploymentName );
        undeploy( supportDeploymentName );
    }

}
