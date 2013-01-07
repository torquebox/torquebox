/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testCamelizeSimple() {
        assertEquals( "FooBar", StringUtils.camelize( "foo_bar" ) );
    }

    @Test
    public void testCamelizeNested() {
        assertEquals( "FooBar::BazCheddar", StringUtils.camelize( "foo_bar/baz_cheddar" ) );
    }

    @Test
    public void testCamelizeSimpleNoOp() {
        assertEquals( "FooBar", StringUtils.camelize( "FooBar" ) );
    }

    @Test
    public void testCamelizeNestedNoOp() {
        assertEquals( "FooBar::BazCheddar", StringUtils.camelize( "FooBar::BazCheddar" ) );
    }

    @Test
    public void testUnderscoreSimple() {
        assertEquals( "foo_bar", StringUtils.underscore( "FooBar" ) );
    }

    @Test
    public void testUnderscoreSimpleNested() {
        assertEquals( "foo_bar/baz_controller", StringUtils.underscore( "FooBar::BazController" ) );
    }

    @Test
    public void testUnderscoreSimpleNoOp() {
        assertEquals( "foo_bar", StringUtils.underscore( "foo_bar" ) );
    }

    @Test
    public void testUnderscoreNestedNoOp() {
        assertEquals( "foo_bar/baz_controller", StringUtils.underscore( "foo_bar/baz_controller" ) );
    }

}
