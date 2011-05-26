package org.torquebox.jobs;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.TorqueBoxYamlParsingProcessor;
import org.torquebox.test.as.AbstractDeploymentProcessorTestCase;
import org.torquebox.test.as.MockDeploymentPhaseContext;
import org.torquebox.test.as.MockDeploymentUnit;

public class JobsYamlParsingProcessorTest extends AbstractDeploymentProcessorTestCase {

    @Before
    public void setUp() {
        appendDeployer( new TorqueBoxYamlParsingProcessor() );
        appendDeployer( new JobsYamlParsingProcessor() );
    }

    /** Ensure that an empty jobs.yml causes no problems. */
    @Test
    public void testEmptyJobsYml() throws Exception {
        URL jobsYml = getClass().getResource( "empty.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", jobsYml );

        deploy( phaseContext );

        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();
        List<ScheduledJobMetaData> allMetaData = unit.getAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY );

        assertTrue( allMetaData.isEmpty() );
    }

    /** Ensure that a valid jobs.yml attaches metadata. */
    @Test
    public void testValidJobsYml() throws Exception {
        URL jobsYml = getClass().getResource( "valid-jobs.yml" );

        MockDeploymentPhaseContext phaseContext = createPhaseContext( "torquebox.yml", jobsYml );

        deploy( phaseContext );

        MockDeploymentUnit unit = phaseContext.getMockDeploymentUnit();

        List<ScheduledJobMetaData> allJobMetaData = unit.getAttachmentList( ScheduledJobMetaData.ATTACHMENTS_KEY );

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
    protected ScheduledJobMetaData getJobMetaData(List<ScheduledJobMetaData> allJobMetaData, String name) {
        for (ScheduledJobMetaData each : allJobMetaData) {
            if (each.getName().equals( name )) {
                return each;
            }
        }
        return null;
    }

}
