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

package org.torquebox.core.util;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.runtime.RuntimeContext;

/**
 * Ruby reflection helper utilities.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class ReflectionHelper {

    /**
     * Set a property on a Ruby object, if possible.
     * 
     * <p>
     * If the target responds to {@code name=}, the property will be set.
     * Otherwise, not.
     * </p>
     * 
     * @param ruby
     *            The Ruby interpreter.
     * @param target
     *            The target object.
     * @param name
     *            The basic name of the property.
     * @param value
     *            The value to attempt to set.
     * @return {@code true} if successful, otherwise {@code false}
     */
    public static boolean setIfPossible(Ruby ruby, Object target, String name, Object value) {
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            boolean success = false;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class );

            if (respondTo.booleanValue()) {
                JavaEmbedUtils.invokeMethod( ruby, target, name + "=", new Object[] { value }, void.class );
            }

            return success;
        } finally {
            RuntimeContext.clearCurrentRuntime();
        }
    }

    public static Object getIfPossible(Ruby ruby, Object target, String name) {
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Object result = null;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

            if (respondTo.booleanValue()) {
                result = JavaEmbedUtils.invokeMethod( ruby, target, name, new Object[] {}, Object.class );
            }

            return result;
        } finally {
            RuntimeContext.clearCurrentRuntime();
        }
    }

    public static Object call(Ruby ruby, Object target, String name, Object[] parameters) {
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            return JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
        } finally {
            RuntimeContext.clearCurrentRuntime();
        }
    }
    
    public static Object callIfPossible(Ruby ruby, Object target, String name, Object[] parameters) {
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Object result = null;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

            if (respondTo.booleanValue()) {
                result = JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
            }

            return result;
        } finally {
            RuntimeContext.clearCurrentRuntime();
        }
    }

    public static IRubyObject instantiate(Ruby ruby, String className) {
        return instantiate( ruby, className, new Object[] {} );
    }

    public static IRubyObject instantiate(Ruby ruby, String className, Object[] parameters) {
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            IRubyObject result = null;
            RubyModule rubyClass = ruby.getClassFromPath( className );

            if (rubyClass != null) {
                result = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", parameters, IRubyObject.class );
            }

            return result;
        } finally {
            RuntimeContext.clearCurrentRuntime();
        }
    }

}
