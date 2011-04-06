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

package org.torquebox.services;

import static org.junit.Assert.*;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.common.reflect.ReflectionHelper;
import org.torquebox.interp.core.RubyComponentResolver;
import org.torquebox.interp.core.RubyRuntimeFactoryImpl;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.test.AbstractTorqueBoxTestCase;

public class RubyServiceProxyTest extends AbstractTorqueBoxTestCase {

    private Ruby ruby;
    private RubyComponentResolver resolver;
    private RubyServiceProxy proxy;

    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();
        this.resolver = new RubyComponentResolver();
        this.proxy = new RubyServiceProxy( resolver, new SharedRubyRuntimePool( this.ruby ) );
    }

    @Test
    public void testServiceStartStop() throws Exception {
        resolver.setRubyClassName( "TestService" );
        resolver.setRubyRequirePath( "org/torquebox/services/test_service" );

        proxy.create();
        proxy.start();
        Boolean started = (Boolean) ReflectionHelper.callIfPossible( this.ruby, proxy.getService(), "started?", null );
        assertTrue( started.booleanValue() );

        proxy.stop();
        started = (Boolean) ReflectionHelper.callIfPossible( this.ruby, proxy.getService(), "started?", null );
        assertFalse( started.booleanValue() );
        proxy.destroy();
    }

    @Test
    public void testInitialization() throws Exception {
        resolver.setRubyClassName( "TestService" );
        resolver.setRubyRequirePath( "org/torquebox/services/test_service" );
        resolver.setInitializeParamsMap( ruby.evalScriptlet( "{'foo'=>42}" ).convertToHash() );

        proxy.create();
        proxy.start();
        assertEquals( 42, JavaEmbedUtils.invokeMethod( ruby, proxy.getService(), "[]", new Object[] { "foo" }, Integer.class ) );
        proxy.stop();
        proxy.destroy();
    }
    
    public Ruby createRuby() throws Exception {
        RubyRuntimeFactoryImpl factory = new RubyRuntimeFactoryImpl();

        if (System.getProperty( "gem.path" ) != null) {
            factory.setGemPath( System.getProperty( "gem.path" ) );
        } else {
            factory.setGemPath( "target/rubygems" );
        }
        factory.setUseJRubyHomeEnvVar( false );

        Ruby ruby = factory.createInstance( getClass().getSimpleName() );

        ruby.evalScriptlet( "require %q(rubygems)" );
        return ruby;
    }

}
