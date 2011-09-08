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

import java.util.concurrent.Callable;

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
    public static boolean setIfPossible(final Ruby ruby, final Object target, final String name, final Object value) {
        return withinContext( ruby, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                boolean success = false;
                Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name + "=" }, Boolean.class );
                if (respondTo.booleanValue()) {
                    JavaEmbedUtils.invokeMethod( ruby, target, name + "=", new Object[] { value }, void.class );
                    success = true;
                }
                return success;
            }
        } );
    }

    public static Object getIfPossible(final Ruby ruby, final Object target, final String name) {
        return withinContext( ruby, new Callable<Object>() {
            public Object call() throws Exception {
                Object result = null;

                Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

                if (respondTo.booleanValue()) {
                    result = JavaEmbedUtils.invokeMethod( ruby, target, name, new Object[] {}, Object.class );
                }
                return result;
            }
        } );
    }

    public static Object call(final Ruby ruby, final Object target, final String name, final Object[] parameters) {
        return withinContext( ruby, new Callable<Object>() {
            public Object call() throws Exception {
                return JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
            }
        } );
    }

    public static Object callIfPossible(final Ruby ruby, final Object target, final String name, final Object[] parameters) {
        return withinContext( ruby, new Callable<Object>() {
            public Object call() throws Exception {
                Object result = null;

                Boolean respondTo = (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );

                if (respondTo.booleanValue()) {
                    result = JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
                }

                return result;
            }
        } );
    }

    public static Object invokeClassMethod(Ruby ruby, String className, String name, Object[] parameters) {
        RubyModule module = ruby.getClassFromPath( className );
        return call( ruby, module, name, parameters );
    }

    public static void require(Ruby ruby, String requirement) {
        evalScriptlet( ruby, "require %q(" + requirement + ")" );
    }

    // ------------------------------------------------------------------------

    public static IRubyObject evalScriptlet(final Ruby ruby, final String script) {
        return withinContext( ruby, new Callable<IRubyObject>() {
            public IRubyObject call() throws Exception {
                return ruby.evalScriptlet( script );
            }
        } );
    }

    public static IRubyObject executeScript(final Ruby ruby, final String script, final String location) {
        return withinContext( ruby, new Callable<IRubyObject>() {
            public IRubyObject call() throws Exception {
                return ruby.executeScript( script, location );
            }
        } );
    }

    // ------------------------------------------------------------------------

    public static IRubyObject instantiate(Ruby ruby, String className) {
        return instantiate( ruby, className, new Object[] {} );
    }

    public static IRubyObject instantiate(final Ruby ruby, final String className, final Object[] parameters) {
        return withinContext( ruby, new Callable<IRubyObject>() {
            public IRubyObject call() throws Exception {
                IRubyObject result = null;
                RubyModule rubyClass = ruby.getClassFromPath( className );

                if (rubyClass != null) {
                    result = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", parameters, IRubyObject.class );
                }

                return result;
            }

        } );
    }

    // ------------------------------------------------------------------------

    public static RubyThread currentThread(Ruby ruby) {
        return (RubyThread) invokeClassMethod( ruby, "Thread", "current", EMPTY_OBJECT_ARRAY );
    }

    // ------------------------------------------------------------------------

    protected static <R> R withinContext(Ruby ruby, Callable<R> block) {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();

        try {
            RuntimeContext.registerRuntime( ruby );
            Thread.currentThread().setContextClassLoader( ruby.getJRubyClassLoader() );
            return block.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException( e );
        } finally {
            Thread.currentThread().setContextClassLoader( originalCl );
        }
    }

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
}
