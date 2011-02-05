package org.torquebox.interp.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.interp.spi.RuntimeInitializer;

public class RubyRuntimeFactoryImplTest {

    @Test
    public void testExplicitClassLoader() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
        ClassLoader cl = new URLClassLoader( new URL[] {} );
        factory.setClassLoader( cl );
        assertSame( cl, factory.getClassLoader() );
    }

    @Test
    public void testContextClassLoader() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
        ClassLoader cl = new URLClassLoader( new URL[] {} );
        Thread.currentThread().setContextClassLoader( cl );
        assertSame( cl, factory.getClassLoader() );
    }

    @Test
    public void testNullInitializerIsAllowed() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.create();
        assertNotNull( ruby );
    }

    @Test
    public void testInitializerIsUsed() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.create();

        assertNotNull( ruby );
        assertSame( ruby, initializer.ruby );
    }

    @Test
    public void testOpenSSL_HMAC_digest() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.create();

        String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
        Object result = ruby.evalScriptlet( script );

        assertNotNull( result );
    }

    @Test
    public void testRubyDefault() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.create();
        assertNotNull( ruby );
        assertFalse( ruby.is1_9() );
    }

    @Test
    public void testRuby18() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.create();
        assertNotNull( ruby );
        assertFalse( ruby.is1_9() );
    }

    // TODO fix this once jruby-complete.jar actually contains 1.9isms.
    @Test
    @Ignore
    public void testRuby19() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        Ruby ruby = factory.create();
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
    }

    @Test
    public void testApplicationEnvironment() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
        Map<String, String> env = new HashMap<String, String>();
        env.put( "CHEESE", "taco" );
        factory.setApplicationEnvironment( env );
        Map<String, String> env2 = factory.createEnvironment();
        assertNotNull( env2.get( "CHEESE" ) );
        assertEquals( env.get( "CHEESE" ), env2.get( "CHEESE" ) );
    }

    @Test
    public void testVersionsDefined() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();
        Ruby ruby = factory.create();

        IRubyObject torqueboxVersion = ruby.evalScriptlet( "TorqueBox.version" );
        assertNotNull( torqueboxVersion );
        assertEquals( System.getProperty( "test.version.torquebox" ), torqueboxVersion.toString() );

        IRubyObject jbossasVersion = ruby.evalScriptlet( "TorqueBox.versions[:jbossas]" );
        assertNotNull( jbossasVersion );
        assertEquals( System.getProperty( "test.version.jbossas" ), jbossasVersion.toString() );

        IRubyObject jrubyVersion = ruby.evalScriptlet( "TorqueBox.versions[:jruby]" );
        assertNotNull( jrubyVersion );
        assertEquals( System.getProperty( "test.version.jruby" ), jrubyVersion.toString() );

    }

    static class MockRuntimeInitializer implements RuntimeInitializer {

        public Ruby ruby;

        @Override
        public void initialize(Ruby ruby) throws Exception {
            this.ruby = ruby;
        }

    }

}
