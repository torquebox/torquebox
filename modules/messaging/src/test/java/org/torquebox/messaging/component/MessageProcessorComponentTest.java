/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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
import org.torquebox.core.util.RuntimeHelper;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class MessageProcessorComponentTest extends AbstractRubyTestCase {
    
    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();

        URL rb = getClass().getResource( "test_message_processor.rb" );
        this.ruby.getLoadService().require( rb.getPath() );

        this.rubyProcessor = RuntimeHelper.instantiate( ruby, "TestMessageProcessor" );
        assertNotNull( rubyProcessor );
    }
    
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testDispatchMessage() throws Exception {
        MessageProcessorComponent processor = new MessageProcessorComponent();
        processor.setRubyComponent( this.rubyProcessor );

        Message message = mock( TextMessage.class );
        processor.process( message );
        
        List messages = (List) RuntimeHelper.getIfPossible( ruby, rubyProcessor, "messages" );
        assertNotNull( messages );
        assertFalse( messages.isEmpty() );
        assertEquals( 1, messages.size() );
        
        Message processedMessage = (Message) RuntimeHelper.getIfPossible( ruby, messages.get( 0 ), "jms_message" );

        assertSame( message, processedMessage );
    }
    
    private Ruby ruby;
    private IRubyObject rubyProcessor;

}
