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

package org.torquebox.rack.core;

import static org.junit.Assert.*;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RackApplicationFactoryImplTest extends AbstractRubyTestCase {

    @Test
    public void testSingleRackup_OneRuntime() throws Exception {
        String rackUpScript = "lambda(){|env|puts env}";
        RackApplicationFactoryImpl appFactory = new RackApplicationFactoryImpl( rackUpScript, VFS.getChild( "/path/to/rackup.ru" ) );

        Ruby ruby = createRuby();
        ruby.evalScriptlet( "RACK_ROOT='/test/app'\n" );
        RackApplication rackApp1 = appFactory.createRackApplication( ruby );
        assertNotNull( rackApp1 );
        RackApplication rackApp2 = appFactory.createRackApplication( ruby );
        assertNotNull( rackApp2 );
        assertSame( rackApp1, rackApp2 );
    }

    @Test
    public void testSingleRackup_TwoRuntimes() throws Exception {
        String rackUpScript = "lambda(){|env|puts env}";
        RackApplicationFactoryImpl appFactory = new RackApplicationFactoryImpl( rackUpScript, VFS.getChild( "/path/to/rackup.ru" ) );

        Ruby ruby1 = createRuby();
        ruby1.evalScriptlet( "RACK_ROOT='/test/app'\n" );
        RackApplication rackApp1a = appFactory.createRackApplication( ruby1 );
        assertNotNull( rackApp1a );
        RackApplication rackApp1b = appFactory.createRackApplication( ruby1 );
        assertNotNull( rackApp1b );
        assertSame( rackApp1a, rackApp1b );

        Ruby ruby2 = createRuby();
        ruby2.evalScriptlet( "RACK_ROOT='/test/app'\n" );
        RackApplication rackApp2a = appFactory.createRackApplication( ruby2 );
        assertNotNull( rackApp2a );
        RackApplication rackApp2b = appFactory.createRackApplication( ruby2 );
        assertNotNull( rackApp2b );
        assertSame( rackApp2a, rackApp2b );

        assertFalse( rackApp1a == rackApp2a );
    }
}
