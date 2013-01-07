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

package org.torquebox.core.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.util.RuntimeHelper;

public class RuntimeContextTest {

    private Ruby runtime1;
    private Ruby runtime2;

    private Map<String, Ruby> results = new HashMap<String, Ruby>();

    @Before
    public void setUpRuntimes() {
        this.runtime1 = Ruby.newInstance();
        this.runtime2 = Ruby.newInstance();
        
        RuntimeContext.registerRuntime( this.runtime1 );
        RuntimeContext.registerRuntime( this.runtime2 );
        this.results.clear();
    }
    
    @After
    public void tearDownRuntimes() {
        RuntimeContext.deregisterRuntime( this.runtime1 );
        RuntimeContext.deregisterRuntime( this.runtime2 );
        
        this.runtime1.tearDown( false );
        this.runtime2.tearDown( false );
    }

    public Ruby getTheRuby() {
        return RuntimeContext.getCurrentRuntime();
    }

    @Test
    public void testStraightContext() throws Exception {
        Thread thread1 = new Thread() {
            public void run() {
                Ruby runtime = (Ruby) RuntimeHelper.call( RuntimeContextTest.this.runtime1, RuntimeContextTest.this, "getTheRuby", null );
                RuntimeContextTest.this.results.put( "one", runtime );
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                Ruby runtime = (Ruby) RuntimeHelper.call( RuntimeContextTest.this.runtime2, RuntimeContextTest.this, "getTheRuby", null );
                RuntimeContextTest.this.results.put( "two", runtime );
            }
        };

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertNotNull( this.results.get( "one" ) );
        assertSame( this.runtime1, this.results.get( "one" ) );

        assertNotNull( this.results.get( "two" ) );
        assertSame( this.runtime2, this.results.get( "two" ) );

    }

    @Test
    public void testWithThreadsGalore() throws Exception {
        Thread thread1 = new Thread() {
            public void run() {
                IRubyObject result = RuntimeHelper.evalScriptlet( runtime1, getScript() );
                
                Object javaResult = JavaEmbedUtils.rubyToJava( result );
                
                RuntimeContextTest.this.results.put(  "we must go deeper", (Ruby) javaResult );
            }
        };

        thread1.start();
        thread1.join();
        
        assertNotNull( this.results.get( "we must go deeper" ) );
        assertSame( this.runtime1, this.results.get( "we must go deeper" ) );

    }
    
    protected String getScript() {
        return "require 'java'\nresult = nil\nThread.new { Thread.new { Thread.new{ result = org.torquebox.core.runtime.RuntimeContext.current_runtime }.join }.join }.join\nresult";
    }

}
