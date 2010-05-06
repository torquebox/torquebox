package org.torquebox.messaging.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.jms.Message;

import org.jruby.Ruby;
import org.jruby.RubySymbol;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyMessageProcessorTest extends AbstractRubyTestCase {
	
	private Ruby ruby;
	private IRubyObject rubyProcessor;

	@Before
	public void setUp() throws Exception {
		this.ruby = createRuby();
		
		URL rb = getClass().getResource( "test_message_processor.rb" );
		this.ruby.getLoadService().require( rb.toString() );
		
		this.rubyProcessor = ReflectionHelper.instantiate( ruby, "TestMessageProcessor" );
		assertNotNull( rubyProcessor );
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureProcessorWithNoConfiguration() throws Exception {
		RubyMessageProcessor processor = new RubyMessageProcessor();
		processor.configureProcessor( this.rubyProcessor );
		
		Map opts = (Map) ReflectionHelper.getIfPossible( this.ruby, this.rubyProcessor, "opts" );
		
		assertNotNull( opts );
		assertTrue( opts.isEmpty() );
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureProcessorWithConfiguration() throws Exception {
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDispatchMessage() throws Exception {
		RubyMessageProcessor processor = new RubyMessageProcessor();
		
		Message message = mock(Message.class);
		processor.processMessage( rubyProcessor, message);
		
		List messages = (List) ReflectionHelper.getIfPossible( ruby, rubyProcessor, "messages" );
		assertNotNull( messages );
		assertFalse( messages.isEmpty() );
		assertEquals( 1, messages.size() );
		
		assertSame( message, messages.get(0) );
	}

}
