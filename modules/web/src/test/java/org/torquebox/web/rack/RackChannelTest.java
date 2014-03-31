/*
 * Copyright 2008-2014 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rack;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class RackChannelTest extends AbstractRubyTestCase {

    @Before
    public void setUp() throws Exception {
        ruby = createRuby();
        rackChannelClass = RackChannel.createRackChannelClass(ruby);
    }

    @Test
    public void testGetsDoesntEatNewlines() {
        String input = "foo\nbar";
        RackChannel channel = new RackChannel(ruby, rackChannelClass, new ByteArrayInputStream(input.getBytes()));
        assertEquals("foo\n", channel.gets(ruby.getCurrentContext()).asJavaString());
        assertEquals("bar", channel.gets(ruby.getCurrentContext()).asJavaString());
    }

    private Ruby ruby;
    private RubyClass rackChannelClass;
}
