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

import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentRegistry {

    public static ComponentRegistry getRegistryFor(Ruby runtime) {
        IRubyObject rubyRegistry = runtime.getObject().getConstant( TORQUEBOX_COMPONENT_REGISTRY );
        return (ComponentRegistry) JavaEmbedUtils.rubyToJava( rubyRegistry );
    }

    public static void createRegistryFor(Ruby runtime) {
        ComponentRegistry javaRegistry = new ComponentRegistry( runtime );
        IRubyObject rubyRegistry = JavaEmbedUtils.javaToRuby( runtime, javaRegistry );
        runtime.getObject().setConstant( TORQUEBOX_COMPONENT_REGISTRY, rubyRegistry );
    }

    private ComponentRegistry(Ruby runtime) {
        this.runtime = runtime;
    }

    public IRubyObject lookup(String componentName) {
        return this.registry.get( componentName );
    }

    public void register(String componentName, IRubyObject rubyComponent) {
        if (rubyComponent.getRuntime() != this.runtime) {
            throw new IllegalArgumentException( "Component/runtime mismatch" );
        }

        this.registry.put( componentName, rubyComponent );
    }

    private Ruby runtime;
    private Map<String, IRubyObject> registry = new HashMap<String, IRubyObject>();

    private static final String TORQUEBOX_COMPONENT_REGISTRY = "TORQUEBOX_COMPONENT_REGISTRY";
}
