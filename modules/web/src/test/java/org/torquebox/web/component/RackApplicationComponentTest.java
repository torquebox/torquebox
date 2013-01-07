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

package org.torquebox.web.component;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.component.ComponentResolver;
import org.torquebox.web.rack.RackEnvironment;

public class RackApplicationComponentTest {
    
    private Ruby ruby;
    private ComponentResolver resolver;
    private IRubyObject rackApp;

    @Before
    public void setUp() {
        this.ruby = Ruby.newInstance();
        
        this.ruby.getLoadService().require(  "org/torquebox/web/component/mock_app" );
        RubyModule mockAppClass = this.ruby.getClassFromPath( "MockApp" );
        this.rackApp = (IRubyObject) JavaEmbedUtils.invokeMethod(  this.ruby, mockAppClass, "new", null, IRubyObject.class );
        
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testCall() throws Exception {
        RackApplicationComponent component = new RackApplicationComponent( this.rackApp );
        RackEnvironment env = mock( RackEnvironment.class );
        RubyHash envHash = RubyHash.newHash( this.ruby );
        when(env.getEnv()).thenReturn( envHash );
        
        Object response = component.call( env );
        
        assertNotNull( response );
        
        RubyHash internalEnvHash = (RubyHash) JavaEmbedUtils.invokeMethod( this.ruby, this.rackApp, "env", null, RubyHash.class );
        
        assertSame( envHash, internalEnvHash );
        
        
    }

}
