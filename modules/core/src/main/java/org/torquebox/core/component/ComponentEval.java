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

public class ComponentEval implements ComponentInstantiator {
    
    private String code;
    private String location;

    public ComponentEval() {

    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    @SuppressWarnings("unchecked")
    public IRubyObject newInstance(Ruby runtime, Object[] initParams) {
        IRubyObject instance = RuntimeHelper.executeScript( runtime, this.code, this.location );
        
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

}
