package org.torquebox.messaging.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.util.List;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.util.ReflectionHelper;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class MessageProcessorComponentTest extends AbstractRubyTestCase {
    
    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();

        URL rb = getClass().getResource( "test_message_processor.rb" );
        this.ruby.getLoadService().require( rb.toString() );

        this.rubyProcessor = ReflectionHelper.instantiate( ruby, "TestMessageProcessor" );
        assertNotNull( rubyProcessor );
    }
    
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testDispatchMessage() throws Exception {
        MessageProcessorComponent processor = new MessageProcessorComponent();
        processor.setRubyComponent( this.rubyProcessor );

        Message message = mock( TextMessage.class );
        processor.process( message );
        
        List messages = (List) ReflectionHelper.getIfPossible( ruby, rubyProcessor, "messages" );
        assertNotNull( messages );
        assertFalse( messages.isEmpty() );
        assertEquals( 1, messages.size() );
        
        Message processedMessage = (Message) ReflectionHelper.getIfPossible( ruby, messages.get( 0 ), "jms_message" );

        assertSame( message, processedMessage );
    }
    
    private Ruby ruby;
    private IRubyObject rubyProcessor;

}
