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

package org.torquebox.interp.core;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.jruby.CompatVersion;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.RubyInstanceConfig.CompileMode;
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
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
    }

    @Test
    public void testInitializerIsUsed() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

        assertNotNull( ruby );
        assertSame( ruby, initializer.ruby );
    }

    @Test
    public void testOpenSSL_HMAC_digest() throws Exception {
        MockRuntimeInitializer initializer = new MockRuntimeInitializer();
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( initializer );
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

        String script = "require 'openssl'\nOpenSSL::HMAC.hexdigest(OpenSSL::Digest::SHA1.new, 'mykey', 'hashme')";
        Object result = ruby.evalScriptlet( script );

        assertNotNull( result );
    }

    @Test
    public void testRubyDefault() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ruby.is1_9() );
    }

    @Test
    public void testRuby18() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_8 );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertFalse( ruby.is1_9() );
    }

    @Test
    public void testRuby19() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        assertEquals( "1.9.2", ((RubyString)ruby.evalScriptlet( "RUBY_VERSION" )).toString() );
    }

    @Test
    public void testCompileModeDefault() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeDefault19() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setRubyVersion( CompatVersion.RUBY1_9 );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertTrue( ruby.is1_9() );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeJIT() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.JIT );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.JIT, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeFORCE() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.FORCE );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.FORCE, ruby.getInstanceConfig().getCompileMode() );
    }

    @Test
    public void testCompileModeOFF() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl( null );
        factory.setUseJRubyHomeEnvVar( false );
        factory.setCompileMode( CompileMode.OFF );
        Ruby ruby = factory.createInstance( getClass().getSimpleName() );
        assertNotNull( ruby );
        assertEquals( CompileMode.OFF, ruby.getInstanceConfig().getCompileMode() );
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

    static class MockRuntimeInitializer implements RuntimeInitializer {

        public Ruby ruby;

        @Override
        public void initialize(Ruby ruby) throws Exception {
            this.ruby = ruby;
        }

    }

}
