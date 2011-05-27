package org.torquebox.core.util;

import java.util.ArrayList;
import java.util.List;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
        ReflectionHelper.setIfPossible( ruby, target, "the_property", value );
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
        ReflectionHelper.setIfPossible( ruby, target, "the_property", value );
        Object fetched = JavaEmbedUtils.invokeMethod( this.ruby, target, "the_property", new Object[] {}, Object.class );
        assertEquals( value, fetched );
    }

}
