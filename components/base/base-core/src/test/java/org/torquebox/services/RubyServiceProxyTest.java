package org.torquebox.services;

import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.interp.core.InstantiatingRubyComponentResolver;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.test.AbstractTorqueBoxTestCase;

public class RubyServiceProxyTest extends AbstractTorqueBoxTestCase {

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
        resolver.setRubyRequirePath( "org/torquebox/services/test_service" );

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
        resolver.setRubyRequirePath( "org/torquebox/services/test_service" );
        resolver.setInitializeParams( ruby.evalScriptlet( "{'foo'=>42}" ).convertToHash() );

        proxy.start();
        assertEquals( 42, JavaEmbedUtils.invokeMethod( ruby, proxy.getService(), "[]", new Object[] { "foo" }, Integer.class ) );
        proxy.stop();
    }
    
    public Ruby createRuby() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();

        if (System.getProperty( "gem.path" ) != null) {
            factory.setGemPath( System.getProperty( "gem.path" ) );
        } else {
            factory.setGemPath( "target/rubygems" );
        }
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.create();

        ruby.evalScriptlet( "require %q(rubygems)" );
        return ruby;
    }

}