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

package org.torquebox.web.rack;

import java.util.Enumeration;
import java.util.Vector;

import org.jboss.vfs.VFS;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RackApplicationImplTest extends AbstractRubyTestCase {

    @Test
    public void testConstruct() throws Exception {
        Ruby ruby = createRuby();
        ruby.evalScriptlet( "RACK_ROOT='/test/app'\n" );

        String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
        RackApplicationImpl rackApp = new RackApplicationImpl( ruby, rackup, VFS.getChild( "/test/path/config.ru" ) );
        IRubyObject rubyApp = rackApp.getRubyApplication();
        assertNotNil( rubyApp );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Enumeration enumeration(Object... values) {
        Vector v = new Vector();
        for (Object each : values) {
            v.add( each );
        }
        return v.elements();
    }
}
