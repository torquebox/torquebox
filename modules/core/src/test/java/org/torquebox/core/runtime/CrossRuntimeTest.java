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

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

public class CrossRuntimeTest {
    
    private Ruby runtime1;
    private Ruby runtime2;

    @Before
    public void setUpRuntimes() {
        this.runtime1 = Ruby.newInstance();
        this.runtime2 = Ruby.newInstance();
    }
    
    @Test
    public void testCrossRuntimes() {
        this.runtime1.evalScriptlet( "require %q(org/torquebox/core/runtime/my_thing)" );
        this.runtime2.evalScriptlet( "require %q(org/torquebox/core/runtime/my_stuff)" );
        
        IRubyObject thing1 = this.runtime1.evalScriptlet( "MyThing.new( 'one' )" );
        IRubyObject thing2 = this.runtime2.evalScriptlet( "MyStuff.new( 'two' )" );
        
        System.err.println( thing1 );
        System.err.println( thing2 );
        
        IRubyObject result = (IRubyObject) JavaEmbedUtils.invokeMethod( thing1.getRuntime(), thing1, "receive", new Object[] { thing2 }, IRubyObject.class );
        
        System.err.println( result.getRuntime() == this.runtime1 ); // false!
        System.err.println( result.getRuntime() == this.runtime2 ); // true!
        
        System.err.print( result );
        
    }

}
