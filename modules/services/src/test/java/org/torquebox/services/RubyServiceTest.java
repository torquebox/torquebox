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

package org.torquebox.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.component.ComponentClass;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.core.runtime.RestartableRubyRuntimePool;
import org.torquebox.core.runtime.RubyRuntimeFactory;
import org.torquebox.core.runtime.SharedRubyRuntimePool;
import org.torquebox.services.component.ServiceComponent;


public class RubyServiceTest {
    
    @Before
    public void setUp() throws Exception {
        this.ruby = createRuby();
        this.componentClass = new ComponentClass();
        this.componentResolver = new ComponentResolver( false );
        this.componentResolver.setComponentInstantiator( this.componentClass );
        this.componentResolver.setComponentWrapperClass( ServiceComponent.class );
        this.service = new RubyService( "test service" );
        this.service.setComponentResolver( this.componentResolver );
        this.service.setRubyRuntimePool( new RestartableRubyRuntimePool(
                new SharedRubyRuntimePool( this.ruby ) ) );
    }
    
    @Test
    public void testServiceStartStop() throws Exception {
        this.componentClass.setClassName( "TestService" );
        this.componentClass.setRequirePath( "org/torquebox/services/test_service" );

        service.create();
        service.start();
        Boolean started = (Boolean) service.getComponent()._callRubyMethod( "started?" );
        assertTrue( started.booleanValue() );

        service.stop();
        started = (Boolean) service.getComponent()._callRubyMethod( "started?" );
        assertFalse( started.booleanValue() );
        service.destroy();
    }
    
    @Test
    public void testInitialization() throws Exception {
        this.componentClass.setClassName( "TestService" );
        this.componentClass.setRequirePath( "org/torquebox/services/test_service" );
        this.componentResolver.setInitializeParams( Collections.singletonMap( "foo", 42 ) );

        assertEquals( "test service", service.getName() );
        service.create();
        service.start();
        Long foo = (Long) service.getComponent()._callRubyMethod( "[]", new Object[] { "foo" } );
        assertEquals( new Long( 42 ), foo );
        String optionsClass = (String) service.getComponent()._callRubyMethod( "options_class_name" );
        assertEquals( "Hash", optionsClass );
        service.stop();
        service.destroy();
    }
    
    @Test
    public void testOnlyCallsStartIfDefined() throws Exception {
        this.componentClass.setClassName( "NoStartService" );
        this.componentClass.setRequirePath( "org/torquebox/services/no_start_service" );
        
        service.create();
        
        // Will throw an exception if we try to call start on the Ruby object
        service.start();
        
        service.stop();
        service.destroy();
    }
    
    @Test
    public void testOnlyCallsStopIfDefined() throws Exception {
        this.componentClass.setClassName( "NoStopService" );
        this.componentClass.setRequirePath( "org/torquebox/services/no_stop_service" );
        
        service.create();
        service.start();
        // Will throw an exception if we try to call stop on the Ruby object
        service.stop();
        
        service.destroy();
    }
    
    @Test
    public void testOnlyStartsOnce() throws Exception {
        this.componentClass.setClassName( "TestService" );
        this.componentClass.setRequirePath( "org/torquebox/services/test_service" );
        service.create();
        
        service.start();
        Long start_count = (Long) service.getComponent()._callRubyMethod( "start_count" );
        assertEquals( new Long( 1 ), start_count );
        
        // Ensure start doesn't actually start it if already started
        service.start();
        start_count = (Long) service.getComponent()._callRubyMethod( "start_count" );
        assertEquals( new Long( 1 ), start_count );
        
        service.stop();
        service.destroy();
    }
    
    protected Ruby createRuby() throws Exception {
        RubyRuntimeFactory factory = new RubyRuntimeFactory();

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
    
    private Ruby ruby;
    private ComponentClass componentClass;
    private ComponentResolver componentResolver;
    private RubyService service;

}
