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

package org.torquebox.core.component;

import static org.junit.Assert.assertEquals;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.RubyRuntimeFactory;

public class ComponentEvalTest {
    
    private RubyRuntimeFactory factory;
    private Ruby ruby;

    @Before
    public void setUpRuby() throws Exception {
        this.factory = new RubyRuntimeFactory();
        this.ruby = this.factory.createInstance( getClass().getSimpleName() );
    }
    
    @After
    public void tearDownRuby() throws Exception {
        this.factory.destroyInstance(  this.ruby  );
        this.ruby = null;
    }
    
    @Test
    public void testNewInstance() throws Exception {
        
        ComponentEval eval = new ComponentEval();
        eval.setCode(  "42"  );
        eval.setLocation( "right here!" );
        
        IRubyObject result = eval.newInstance( this.ruby, new Object[] {}, false  );
        
        Long javaResult = (Long) JavaEmbedUtils.rubyToJava( result );
        
        assertEquals( 42, javaResult.longValue() );
    }

}
