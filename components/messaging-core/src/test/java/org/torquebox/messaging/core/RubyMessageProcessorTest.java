package org.torquebox.messaging.core;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubySymbol;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyMessageProcessorTest extends AbstractRubyTestCase {
	
	private Ruby ruby;

	@Before
	public void setUp() throws Exception {
		this.ruby = createRuby();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureProcessorWithNoConfiguration() throws Exception {
		
		URL rb = getClass().getResource( "test_message_processor.rb" );
		this.ruby.getLoadService().require( rb.toString() );
		
		IRubyObject rubyProcessor = ReflectionHelper.instantiate( ruby, "TestMessageProcessor" );
		
		assertNotNull( rubyProcessor );
		
		RubyMessageProcessor processor = new RubyMessageProcessor();
		processor.configureProcessor( rubyProcessor );
		
		Map opts = (Map) ReflectionHelper.getIfPossible( ruby, rubyProcessor, "opts" );
		
		assertNotNull( opts );
		assertTrue( opts.isEmpty() );
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureProcessorWithConfiguration() throws Exception {
		
		URL rb = getClass().getResource( "test_message_processor.rb" );
		this.ruby.getLoadService().require( rb.toString() );
		
		IRubyObject rubyProcessor = ReflectionHelper.instantiate( ruby, "TestMessageProcessor" );
		
		assertNotNull( rubyProcessor );
		
		RubyMessageProcessor processor = new RubyMessageProcessor();
		IRubyObject rubyConfig = ruby.evalScriptlet( "TestMessageProcessor::CONFIG_ONE" );
		processor.setRubyConfig( (String) rubyConfig.toJava( String.class ) );
		processor.configureProcessor( rubyProcessor );
		
		Map opts = (Map) ReflectionHelper.getIfPossible( ruby, rubyProcessor, "opts" );
		
		assertNotNull( opts );
		assertFalse( opts.isEmpty() );
		
		assertEquals( "cheese", opts.get( RubySymbol.newSymbol( ruby, "prop1" ) ) );
		assertEquals( 42L, opts.get( RubySymbol.newSymbol( ruby, "prop2" ) ) );
	}

}
