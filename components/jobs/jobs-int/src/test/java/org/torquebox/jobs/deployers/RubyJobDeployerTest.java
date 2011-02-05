package org.torquebox.jobs.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.core.ScheduledJob;
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

        ScheduledJobMetaData jobMetaData = new ScheduledJobMetaData();
        jobMetaData.setName( "job.one" );
        jobMetaData.setDescription( "test job" );
        jobMetaData.setRubyClassName( "TestJob" );
        jobMetaData.setCronExpression( "22 * * * * ?" );

        jobMetaData.setRubySchedulerName( "scheduler" );

        String deploymentName = createDeployment( "simple" );
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        AttachmentUtils.multipleAttach( unit, jobMetaData, "job.one" );

        processDeployments( true );

        String jobBeanName = AttachmentUtils.beanName( unit, ScheduledJob.class, "job.one" );
        BeanMetaData jobBeanMetaData = getBeanMetaData( unit, jobBeanName );
        assertNotNull( jobBeanMetaData );

        ScheduledJob jobBean = (ScheduledJob) getBean( jobBeanName );
        assertNotNull( jobBean );
        assertEquals( "job.one", jobBean.getName() );
        assertEquals( "22 * * * * ?", jobBean.getCronExpression() );
        assertEquals( "TestJob", jobBean.getRubyClassName() );

        undeploy( deploymentName );
        undeploy( supportDeploymentName );
    }

}
