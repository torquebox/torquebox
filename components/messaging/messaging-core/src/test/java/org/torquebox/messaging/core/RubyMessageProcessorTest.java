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

package org.torquebox.messaging.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.jruby.Ruby;
import org.jruby.RubyHash;
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

        RubyHash rubyConfig = (RubyHash) ruby.evalScriptlet( "TestMessageProcessor::CONFIG_ONE" );
        processor.setRubyConfig( rubyConfig );

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

        Message message = mock( TextMessage.class );
        processor.processMessage( rubyProcessor, message );

        List messages = (List) ReflectionHelper.getIfPossible( ruby, rubyProcessor, "messages" );
        assertNotNull( messages );
        assertFalse( messages.isEmpty() );
        assertEquals( 1, messages.size() );

        assertSame( message, messages.get( 0 ) );
    }

}
