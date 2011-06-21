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

import java.util.HashMap;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentRegistry {

    public static ComponentRegistry getRegistryFor(Ruby runtime) {
        IRubyObject rubyRegistry = null;
        synchronized (runtime) {
            try {
                rubyRegistry = runtime.getObject().getConstant( TORQUEBOX_COMPONENT_REGISTRY );
            } catch (RaiseException e) {
                //e.printStackTrace();
            }

            ComponentRegistry javaRegistry = null;

            if (rubyRegistry == null || rubyRegistry.isNil()) {
                javaRegistry = new ComponentRegistry( runtime );
                rubyRegistry = JavaEmbedUtils.javaToRuby( runtime, javaRegistry );
                runtime.getObject().setConstant( TORQUEBOX_COMPONENT_REGISTRY, rubyRegistry );
                System.err.println( "DEFINE COMPONENT_REGISTRY for " + runtime + " by " + Thread.currentThread() );
            } else {
                javaRegistry = (ComponentRegistry) JavaEmbedUtils.rubyToJava( rubyRegistry );
            }

            return javaRegistry;
        }
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
