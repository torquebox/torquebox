package org.torquebox.jobs.deployers;

import java.net.URL;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.jobs.metadata.RubyJobMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import static org.junit.Assert.*;

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
        URL jobsYml = getClass().getResource("empty-jobs.yml");
        
        String deploymentName = addDeployment(jobsYml, "jobs.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        assertNotNull( unit );
        
        Set<? extends RubyJobMetaData> allJobMetaData = unit.getAllMetaData( RubyJobMetaData.class );
        
        assertNotNull( allJobMetaData );
        assertTrue( allJobMetaData.isEmpty() );
	}
	
	/** Ensure that a valid jobs.yml attaches metadata. */
	@Test
	public void testValidJobsYml() throws Exception {
        URL jobsYml = getClass().getResource("valid-jobs.yml");
        
        String deploymentName = addDeployment(jobsYml, "jobs.yml");
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        assertNotNull( unit );
        
        Set<? extends RubyJobMetaData> allJobMetaData = unit.getAllMetaData( RubyJobMetaData.class );
        
        assertNotNull( allJobMetaData );
        assertEquals( 2, allJobMetaData.size() );
        
        RubyJobMetaData jobOne = getJobMetaData(allJobMetaData, "job.one" );
        assertNotNull( jobOne );
        assertEquals( "job.one", jobOne.getName() );
        assertEquals( "My Job is routine", jobOne.getDescription() );
        assertEquals( "01 * * * * ?", jobOne.getCronExpression() );
        assertEquals( "MyJobClass", jobOne.getRubyClassName() );
        assertNotNull( jobOne.getGroup() );
        
        RubyJobMetaData jobTwo = getJobMetaData(allJobMetaData, "job.two" );
        assertNotNull( jobTwo );
        assertEquals( "job.two", jobTwo.getName() );
        assertEquals( "My other Job is extraodinary", jobTwo.getDescription() );
        assertEquals( "01 01 01 15 * ?", jobTwo.getCronExpression() );
        assertEquals( "MyOtherJobClass", jobTwo.getRubyClassName() );
        assertNotNull( jobTwo.getGroup() );
        
        assertEquals( jobOne.getGroup(), jobTwo.getGroup() );
	}
	
	/** Locate a RubyJobMetaData given a collection and a name to search for.
	 * 
	 * @param allJobMetaData The collection
	 * @param name The search name
	 * @return The found metadata, or null if no matching are found.
	 */
	protected RubyJobMetaData getJobMetaData(Set<? extends RubyJobMetaData> allJobMetaData, String name) {
		for ( RubyJobMetaData each : allJobMetaData ) {
			if ( each.getName().equals( name ) ) {
				return each;
			}
		}
		return null;
	}

}
