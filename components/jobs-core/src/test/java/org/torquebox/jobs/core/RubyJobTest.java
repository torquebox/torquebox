package org.torquebox.jobs.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyJobTest extends AbstractRubyTestCase {
	
	private RubyRuntimePool runtimePool;
	private Scheduler scheduler;

	@Before
	public void setUp() throws Exception {
		this.runtimePool = mock( RubyRuntimePool.class );
		this.scheduler = mock( Scheduler.class );
	}
	
	@Test
	public void testBasics() throws Exception {
		RubyJob rubyJob = new RubyJob();
		rubyJob.setName( "job.one" );
		rubyJob.setDescription( "Quality is Job One." );
		rubyJob.setCronExpression( "*/2 * * * * ?");
		rubyJob.setRubyClassName( "JobOne" );
		rubyJob.setRubyRuntimePool( this.runtimePool );
		rubyJob.setScheduler( this.scheduler );
		
		rubyJob.start();
		verify( this.scheduler ).scheduleJob( argThat(new JobDetailMatcher( rubyJob ) ), any( Trigger.class ) );
	}
	
	class JobDetailMatcher extends TypeSafeMatcher<JobDetail> {
		
		private RubyJob rubyJob;

		public JobDetailMatcher(RubyJob rubyJob) {
			this.rubyJob = rubyJob;
		}

		@Override
		public boolean matchesSafely(JobDetail item) {
			return ( 
					item.getName().equals( rubyJob.getName() )
					&&
					item.getDescription().equals( rubyJob.getDescription() )
					&&
					item.getJobClass().equals( RubyJobHandler.class )
					&&
					item.getJobDataMap().getString( RubyJob.RUBY_CLASS_NAME_KEY ).equals( rubyJob.getRubyClassName() )
					);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText( "JobDetail equivelant of: " + this.rubyJob.toString() );
		}
		
	}
	

}
