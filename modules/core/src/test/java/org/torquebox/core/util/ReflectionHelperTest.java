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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Before;
import org.junit.Test;

public class ReflectionHelperTest {

    private Ruby ruby;

    @Before
    public void setUpRuby() {
        List<String> loadPaths = new ArrayList<String>();
        this.ruby = JavaEmbedUtils.initialize( loadPaths );
    }

    @Test
    public void testSetIfPossible_Unsettable() {
        Object value = "beans";
        ruby.loadFile( "unsettable.rb", getClass().getResourceAsStream( "unsettable.rb" ), false );
        RubyClass cls = (RubyClass) ruby.getClassFromPath( "Unsettable" );
        assertNotNull( cls );
        Object target = JavaEmbedUtils.invokeMethod( this.ruby, cls, "new", new Object[] {}, Object.class );
        assertNotNull( target );
        RuntimeHelper.setIfPossible( ruby, target, "the_property", value );
        Object fetched = JavaEmbedUtils.invokeMethod( this.ruby, target, "the_property", new Object[] {}, Object.class );
        assertEquals( "unsettable", fetched );
    }

    @Test
    public void testSetIfPossible_Settable() {
        Object value = "beans";
        ruby.loadFile( "settable.rb", getClass().getResourceAsStream( "settable.rb" ), false );
        RubyClass cls = (RubyClass) ruby.getClassFromPath( "Settable" );
        assertNotNull( cls );
        Object target = JavaEmbedUtils.invokeMethod( this.ruby, cls, "new", new Object[] {}, Object.class );
        assertNotNull( target );
        RuntimeHelper.setIfPossible( ruby, target, "the_property", value );
        Object fetched = JavaEmbedUtils.invokeMethod( this.ruby, target, "the_property", new Object[] {}, Object.class );
        assertEquals( value, fetched );
    }

}
