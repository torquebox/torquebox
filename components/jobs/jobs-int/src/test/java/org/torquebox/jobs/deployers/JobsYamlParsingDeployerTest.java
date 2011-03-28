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

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.metadata.ScheduledJobMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class JobsYamlParsingDeployerTest extends AbstractDeployerTestCase {

    private JobsYamlParsingDeployer deployer;

    @Before
    public void setUp() throws Throwable {
        this.deployer = new JobsYamlParsingDeployer();
        addDeployer( this.deployer );
    }

    /** Ensure that an empty jobs.yml causes no problems. */
    @Test
    public void testEmptyJobsYml() throws Exception {
        URL jobsYml = getClass().getResource( "empty.yml" );

        String deploymentName = addDeployment( jobsYml, "jobs.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        assertNotNull( unit );

        Set<? extends ScheduledJobMetaData> allJobMetaData = unit.getAllMetaData( ScheduledJobMetaData.class );

        assertNotNull( allJobMetaData );
        assertTrue( allJobMetaData.isEmpty() );
    }

    /** Ensure that a valid jobs.yml attaches metadata. */
    @Test
    public void testValidJobsYml() throws Exception {
        URL jobsYml = getClass().getResource( "valid-jobs.yml" );

        String deploymentName = addDeployment( jobsYml, "jobs.yml" );
        processDeployments( true );

        DeploymentUnit unit = getDeploymentUnit( deploymentName );

        assertNotNull( unit );

        Set<? extends ScheduledJobMetaData> allJobMetaData = unit.getAllMetaData( ScheduledJobMetaData.class );

        assertNotNull( allJobMetaData );
        assertEquals( 3, allJobMetaData.size() );

        ScheduledJobMetaData jobOne = getJobMetaData( allJobMetaData, "job.one" );
        assertNotNull( jobOne );
        assertEquals( "job.one", jobOne.getName() );
        assertEquals( "My Job is routine", jobOne.getDescription() );
        assertEquals( "01 * * * * ?", jobOne.getCronExpression() );
        assertEquals( "MyJobClass", jobOne.getRubyClassName() );
        assertFalse( jobOne.isSingleton() );
        assertNotNull( jobOne.getGroup() );

        ScheduledJobMetaData jobTwo = getJobMetaData( allJobMetaData, "job.two" );
        assertNotNull( jobTwo );
        assertEquals( "job.two", jobTwo.getName() );
        assertEquals( "My other Job is extraodinary", jobTwo.getDescription() );
        assertEquals( "01 01 01 15 * ?", jobTwo.getCronExpression() );
        assertEquals( "MyOtherJobClass", jobTwo.getRubyClassName() );
        assertFalse( jobTwo.isSingleton() );
        assertNotNull( jobTwo.getGroup() );
        
        ScheduledJobMetaData jobThree = getJobMetaData( allJobMetaData, "job.three" );
        assertNotNull( jobThree );
        assertEquals( "job.three", jobThree.getName() );
        assertEquals( "My singleton job class", jobThree.getDescription() );
        assertEquals( "01 01 01 15 * ?", jobThree.getCronExpression() );
        assertEquals( "SingletonJobClass", jobThree.getRubyClassName() );
        assertTrue( jobThree.isSingleton() );
        assertNotNull( jobTwo.getGroup() );        

        assertEquals( jobOne.getGroup(), jobTwo.getGroup() );
        assertEquals( jobOne.getGroup(), jobThree.getGroup() );
    }

    /**
     * Locate a RubyJobMetaData given a collection and a name to search for.
     * 
     * @param allJobMetaData
     *            The collection
     * @param name
     *            The search name
     * @return The found metadata, or null if no matching are found.
     */
    protected ScheduledJobMetaData getJobMetaData(Set<? extends ScheduledJobMetaData> allJobMetaData, String name) {
        for (ScheduledJobMetaData each : allJobMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }
        return null;
    }

}
