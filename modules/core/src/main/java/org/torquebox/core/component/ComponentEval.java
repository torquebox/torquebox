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

package org.torquebox.core.component;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

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
    
    public IRubyObject newInstance(Ruby runtime, Object[] initParams) {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader( runtime.getJRubyClassLoader().getParent() );
            IRubyObject component = runtime.executeScript( this.code, this.location );
            return component;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }
    
    

}
