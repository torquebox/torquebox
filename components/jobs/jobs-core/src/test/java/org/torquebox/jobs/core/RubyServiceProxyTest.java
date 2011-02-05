package org.torquebox.jobs.core;

import org.junit.*;
import java.net.*;
import java.util.*;
import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.javasupport.JavaEmbedUtils;

import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.test.ruby.AbstractRubyTestCase;
import org.torquebox.common.reflect.ReflectionHelper;

public class RubyServiceProxyTest extends AbstractRubyTestCase {

    private Ruby ruby;
    private InstantiatingRubyComponentResolver resolver;
    private RubyServiceProxy proxy;

    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();
        this.resolver = new InstantiatingRubyComponentResolver();
        this.proxy = new RubyServiceProxy( resolver, new SharedRubyRuntimePool( this.ruby ) );
    }

    @Test
    public void testServiceStartStop() throws Exception {
        resolver.setRubyClassName( "TestService" );
        resolver.setRubyRequirePath( "org/torquebox/jobs/core/test_service" );

        proxy.start();
        Boolean started = (Boolean) ReflectionHelper.callIfPossible( this.ruby, proxy.getService(), "started?", null );
        assertTrue( started.booleanValue() );

        proxy.stop();
        started = (Boolean) ReflectionHelper.callIfPossible( this.ruby, proxy.getService(), "started?", null );
        assertFalse( started.booleanValue() );
    }

    @Test
    public void testInitialization() throws Exception {
        resolver.setRubyClassName( "TestService" );
        resolver.setRubyRequirePath( "org/torquebox/jobs/core/test_service" );
        resolver.setInitializeParams( ruby.evalScriptlet( "{'foo'=>42}" ).convertToHash() );

        proxy.start();
        assertEquals( 42, JavaEmbedUtils.invokeMethod( ruby, proxy.getService(), "[]", new Object[] { "foo" }, Integer.class ) );
        proxy.stop();
    }

}