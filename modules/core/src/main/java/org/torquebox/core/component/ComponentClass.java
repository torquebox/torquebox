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

package org.torquebox.core.component;

import java.lang.reflect.Field;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.util.RuntimeHelper;

public class ComponentClass implements ComponentInstantiator {

    private String className;
    private String requirePath;

    public ComponentClass() {

    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setRequirePath(String requirePath) {
        this.requirePath = requirePath;
    }

    public String getRequirePath() {
        return this.requirePath;
    }

    @SuppressWarnings("unchecked")
    public IRubyObject newInstance(Ruby runtime, Object[] initParams) {
        if (this.requirePath != null) {
            runtime.getLoadService().load( this.requirePath + ".rb", false );
        }
        
        IRubyObject instance = RuntimeHelper.instantiate( runtime, this.className, initParams );
        
        //
        // HACK - Remove once upgraded to JRuby 1.6.7
        //
        try {
            Field recursiveField = runtime.getClass().getDeclaredField( "recursive" );
            recursiveField.setAccessible( true );
            ((ThreadLocal<Map<String, RubyHash>>) recursiveField.get( runtime )).remove();
        }
        catch (Exception ex) {
            // safe to ignore
        }
        // END HACK
        
        return instance;
    }
    
    public String toString() {
        return this.className;
    }

}
