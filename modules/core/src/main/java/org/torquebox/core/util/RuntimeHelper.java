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
import org.jruby.RubyThread;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.runtime.RuntimeContext;

/**
 * Ruby reflection helper utilities.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RuntimeHelper {

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
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            boolean success = false;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class );

            if (respondTo.booleanValue()) {
                JavaEmbedUtils.invokeMethod( ruby, target, name + "=", new Object[] { value }, void.class );
            }

            return success;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    public static Object getIfPossible(Ruby ruby, Object target, String name) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            Object result = null;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

            if (respondTo.booleanValue()) {
                result = JavaEmbedUtils.invokeMethod( ruby, target, name, new Object[] {}, Object.class );
            }

            return result;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    public static Object call(Ruby ruby, Object target, String name, Object[] parameters) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            return JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    public static Object callIfPossible(Ruby ruby, Object target, String name, Object[] parameters) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            Object result = null;

            Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

            if (respondTo.booleanValue()) {
                result = JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
            }

            return result;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    public static Object invokeClassMethod(Ruby ruby, String className, String name, Object[] parameters) {
        RubyModule module = ruby.getClassFromPath( className );
        return call( ruby, module, name, parameters );
    }

    public static void require(Ruby ruby, String requirement) {
        evalScriptlet( ruby, "require %q(" + requirement + ")" );
    }

    public static IRubyObject evalScriptlet(Ruby ruby, String script) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            return ruby.evalScriptlet( script );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }
    
    public static IRubyObject executeScript(Ruby ruby, String script, String location) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            return ruby.executeScript( script, location );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    public static IRubyObject instantiate(Ruby ruby, String className) {
        return instantiate( ruby, className, new Object[] {} );
    }

    public static RubyThread currentThread(Ruby ruby) {
        return (RubyThread) invokeClassMethod( ruby, "Thread", "current", EMPTY_OBJECT_ARRAY );
    }

    public static IRubyObject instantiate(Ruby ruby, String className, Object[] parameters) {
        Ruby originalRuby = RuntimeContext.getCurrentRuntime();
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            RuntimeContext.setCurrentRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            IRubyObject result = null;
            RubyModule rubyClass = ruby.getClassFromPath( className );

            if (rubyClass != null) {
                result = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", parameters, IRubyObject.class );
            }

            return result;
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
            RuntimeContext.setCurrentRuntime( originalRuby );
        }
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
}
