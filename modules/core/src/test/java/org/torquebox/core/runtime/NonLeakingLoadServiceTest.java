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

import static org.junit.Assert.assertNotNull;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.LoadService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NonLeakingLoadServiceTest {

    private LoadService loadService;
    private Ruby ruby;

    @Before
    public void setUp() throws Exception {
        RubyRuntimeFactory factory = new RubyRuntimeFactory();
        factory.setUseJRubyHomeEnvVar( false );
        this.ruby = factory.createInstance( "test" );
        this.loadService = ruby.getLoadService();
    }

    @Test
    public void testRubygemsLoadable() {
        this.loadService.require( "rubygems" );
        IRubyObject result = this.ruby.evalScriptlet( "Gem" );
        assertNotNull( result );
    }

    @Test
    public void testClasspathLoadable() {
        this.loadService.require(  "dummy" );
        IRubyObject result = this.ruby.evalScriptlet( "Dummy" );
        assertNotNull( result );
    }

    @Test
    @Ignore( "only run locally" ) 
    /**
     * Used only in manual testing to verify we're not leaking input streams
     * when loading dummy.rb - lsof -p <pid> of test process to see number of
     * references to dummy.rb. It's always 0 when fixed, double digits when leaking.
     */
    public void testNoLeaks() throws Exception {
        for(int i = 0; i < 100000; i++) {
            loadService.require( "dummy.rb" );
            loadService.removeInternalLoadedFeature( "dummy.rb" );
        }
    }

}
