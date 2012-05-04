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

package org.torquebox.core.runtime;

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
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Test;
import org.torquebox.core.util.JRubyConstants;

public class RubyRuntimeFactoryTest {

    private RubyRuntimeFactory factory;

    @After
    public void destroyFactory() {
        if (factory != null) {
            this.factory.destroy();
            this.factory = null;
        }

    }

    @Test
    public void testExplicitClassLoader() throws Exception {
        factory = new RubyRuntimeFactory();
        ClassLoader cl = new URLClassLoader( new URL[] {} );
        factory.setClassLoader( cl );
        factory.create();
        assertSame( cl, factory.getClassLoader() );
    }

    @Test
    public void testContextClassLoader() throws Exception {
        factory = new RubyRuntimeFactory();
        ClassLoader cl = new URLClassLoader( new URL[] {} );
        Thread.currentThread().setContextClassLoader( cl );
        factory.create();
        assertSame( cl, factory.getClassLoader() );
    }

    @Test
    public void testNullInitializerIsAllowed() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        factory.create();
        assertNotNull( ruby );
    }

    @Test
    public void testInitializerIsUsed() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        factory = new RubyRuntimeFactory( initializer );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();

        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

        assertNotNull( ruby );
        assertSame( ruby, initializer.ruby );
    }

    @Test
    public void testOpenSSL_HMAC_digest() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        factory = new RubyRuntimeFactory( initializer );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();

        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

        String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
        Object result = ruby.evalScriptlet( script );

        assertNotNull( result );
    }

    @Test
    public void testRubyDefault() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        if (isJRuby17()) {
            assertTrue( ruby.is1_9() );
        } else {
            assertFalse( ruby.is1_9() );
        }
    }

    @Test
    public void testRuby18() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_8 );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ruby.is1_9() );
    }

    @Test
    public void testRuby19() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        String rubyVersion = ((RubyString) ruby.evalScriptlet( "RUBY_VERSION" )).toString();
        if (isJRuby17()) {
            assertEquals( "1.9.3", rubyVersion );
        } else {
            assertEquals( "1.9.2", rubyVersion );
        }
    }

    @Test
    public void testCompileModeDefault() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeDefault19() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeJIT() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.JIT );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeFORCE() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.FORCE );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.FORCE, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeOFF() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.OFF );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.OFF, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testApplicationEnvironment() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        factory = new RubyRuntimeFactory( initializer );
        Map<String, String> env = new HashMap<String, String>();
        env.put( "CHEESE", "taco" );
        factory.setApplicationEnvironment( env );
        Map<String, String> env2 = factory.createEnvironment();
        factory.create();
        assertNotNull( env2.get( "CHEESE" ) );
        assertEquals( env.get( "CHEESE" ), env2.get( "CHEESE" ) );
    }

    @Test
    public void testVersionsDefined() throws Exception {
        factory = new RubyRuntimeFactory();
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

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

    @Test
    public void testDebugModeDefault() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ruby.getInstanceConfig().isDebug() );
    }

    @Test
    public void testDebugModeDefault19() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        assertFalse( ruby.getInstanceConfig().isDebug() );
    }

    @Test
    public void testDebugModeTrue() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setDebug( true );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.getInstanceConfig().isDebug() );
    }


    @Test
    public void testDebugModeFalse() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setDebug( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ruby.getInstanceConfig().isDebug() );
    }

    @Test
    public void testInteractiveDefault() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ((TorqueBoxRubyInstanceConfig) ruby.getInstanceConfig()).isInteractive() );
    }

    @Test
    public void testInteractiveDefault19() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        assertFalse( ((TorqueBoxRubyInstanceConfig) ruby.getInstanceConfig()).isInteractive() );
    }

    @Test
    public void testInteractiveTrue() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setInteractive( true );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ((TorqueBoxRubyInstanceConfig) ruby.getInstanceConfig()).isInteractive() );
    }


    @Test
    public void testInteractiveFalse() throws Exception {
        factory = new RubyRuntimeFactory( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setInteractive( false );
        factory.create();
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ((TorqueBoxRubyInstanceConfig) ruby.getInstanceConfig()).isInteractive() );
    }

    public boolean isJRuby17() {
        return JRubyConstants.getVersion().startsWith( "1.7" );
    }

    static class MockRuntimeInitializer implements RuntimeInitializer {

        public Ruby ruby;

        @Override
        public void initialize(Ruby ruby, String runtimeContext) throws Exception {
            this.ruby = ruby;
        }

    }

}
