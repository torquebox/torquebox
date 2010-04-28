package org.torquebox.jobs.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;

import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyJobHandlerTest extends AbstractRubyTestCase {
	
	private Ruby ruby;
	private SharedRubyRuntimePool rubyRuntimePool;

	@Before
	public void setUpRuby() throws Exception {
		this.ruby = createRuby();
		URL jobDefinitionsUrl = getClass().getResource( "job-definitions.rb" );
		System.err.println( "DEF [" + jobDefinitionsUrl.getPath() + "]" );
		this.ruby.getLoadService().require( jobDefinitionsUrl.getPath() );
		this.rubyRuntimePool = new SharedRubyRuntimePool( this.ruby );
	}

	@Test
	public void testBasics() throws Exception {
		RubyJobHandler handler = new RubyJobHandler();
		
		JobExecutionContext context = mock( JobExecutionContext.class );
		JobDetail jobDetail = mock( JobDetail.class );
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put( RubyJob.RUNTIME_POOL_KEY, this.rubyRuntimePool );
		jobDataMap.put( RubyJob.RUBY_CLASS_NAME_KEY, "MyFirstJob" );
		
		final ObjectHolder stashedResult = new ObjectHolder();
		
		doAnswer( new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object result = invocation.getArguments()[0];
				System.err.println( result );
				stashedResult.object = result;
				return null;
			}
			
		} ).when( context ).setResult(any(Object.class));
		
		when( context.getJobDetail() ).thenReturn( jobDetail );
		when ( jobDetail.getJobDataMap() ).thenReturn( jobDataMap );
		
		handler.execute( context );
		
		Object jobResult = stashedResult.object;
		
		assertNotNull( jobResult );
		assertEquals( 42L, jobResult );
	}
	
	public class ObjectHolder {
		public Object object;
	}
	
}
