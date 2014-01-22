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

package org.torquebox.core.util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyModule;
import org.jruby.RubyThread;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.JRubyClassLoader;

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
                if (defined( ruby, target, name + "=" )) {
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
                if (defined( ruby, target, name )) {
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
                if (defined( ruby, target, name )) {
                    result = JavaEmbedUtils.invokeMethod( ruby, target, name, parameters, Object.class );
                }

                return result;
            }
        } );
    }

    public static boolean defined(final Ruby ruby, final Object target, final String name) {
        return withinContext( ruby, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return (Boolean) JavaEmbedUtils.invokeMethod( ruby, target, "respond_to?", new Object[] { name }, Boolean.class );
            }
        } );
    }

    public static Object invokeClassMethod(Ruby ruby, String className, String name, Object[] parameters) {
        RubyModule module = ruby.getClassFromPath( className );
        return call( ruby, module, name, parameters );
    }

    public static void require(Ruby ruby, String requirement) {
        try {
            evalScriptlet( ruby, "require %q(" + requirement + ")" );
        } catch (Throwable t) {
            log.errorf( t, "Unable to require file: %s", requirement );
        }
    }

    /**
     * Looks in application ROOT/config and application ROOT for configurationFile
     * and requires it if possible.
     */
    public static void requireTorqueBoxInit(Ruby ruby) {
      RuntimeHelper.requireIfAvailable( ruby, "config" + File.separator + "torquebox_init", false );
      RuntimeHelper.requireIfAvailable( ruby, "torquebox_init", false );
    }

    public static boolean requireIfAvailable(Ruby ruby, String requirement) {
        return requireIfAvailable( ruby, requirement, true );
    }
    
    /**
     * Calls "require 'requirement'" in the Ruby provided.
     * @returns boolean If successful, returns true, otherwise false.
     */
    public static boolean requireIfAvailable(Ruby ruby, String requirement, boolean logErrors) {
        boolean success = false;
        try {
            StringBuilder script = new StringBuilder();
            script.append("require %q(");
            script.append(requirement);
            script.append(")\n");
            evalScriptlet( ruby, script.toString(), false );
            success = true;
        } catch (Throwable t) {
            success = false;
            if (logErrors) {
                log.debugf( t, "Error encountered. Unable to require file: %s", requirement );
            }
        }
        return success;
    }

    public static void requireUnlessDefined(Ruby ruby, String requirement, String constant) {
        try {
            evalScriptlet( ruby, "require %q(" + requirement + ") unless defined?(" + constant + ")" );
        } catch (Throwable t) {
            log.errorf( t, "Unable to require file: %s", requirement );
        }
    }

    // ------------------------------------------------------------------------

    public static IRubyObject evalScriptlet(final Ruby ruby, final String script) {
        return evalScriptlet( ruby, script, true );
    }

    public static IRubyObject evalScriptlet(final Ruby ruby, final String script, final boolean logErrors) {
        return withinContext( ruby, new Callable<IRubyObject>() {
            public IRubyObject call() throws Exception {
                try {
                    return ruby.evalScriptlet( script );
                } catch (Exception e) {
                    if (logErrors)
                        log.errorf( e, "Error during evaluation: %s", script );
                    throw e;
                }
            }
        } );
    }

    public static IRubyObject executeScript(final Ruby ruby, final String script, final String location) {
        return withinContext( ruby, new Callable<IRubyObject>() {
            public IRubyObject call() throws Exception {
                try {
                    return ruby.executeScript( script, location );
                } catch (Exception e) {
                    log.errorf( e, "Error during execution: %s", script );
                    throw e;
                }
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
                    try {
                    result = (IRubyObject) JavaEmbedUtils.invokeMethod( ruby, rubyClass, "new", parameters, IRubyObject.class );
                    } catch (Exception e) {
                        log.errorf( e, "Unable to instantiate: %s", className );
                        throw e;
                    }
                }

                return result;
            }

        } );
    }

    // ------------------------------------------------------------------------

    public static RubyThread currentThread(Ruby ruby) {
        return (RubyThread) invokeClassMethod( ruby, "Thread", "current", EMPTY_OBJECT_ARRAY );
    }

    @SuppressWarnings("rawtypes")
    public static RubyHash convertJavaMapToRubyHash(Ruby runtime, Map map) {
        RubyHash rubyHash = RubyHash.newHash( runtime );
        for (Object object : map.entrySet()) {
            Entry entry = (Entry) object;
            rubyHash.put( entry.getKey(), entry.getValue() );
        }
        return rubyHash;
    }

    // ------------------------------------------------------------------------

    protected static <R> R withinContext(Ruby ruby, Callable<R> block) {
        WeakReference<ClassLoader> weakRef = jrubyClassLoaders.get(ruby);
        ClassLoader jrubyClassLoader = weakRef == null ? null : weakRef.get();
        if (jrubyClassLoader == null) {
            synchronized(jrubyClassLoaders) {
                jrubyClassLoader = ruby.getJRubyClassLoader();
                jrubyClassLoaders.put(ruby, new WeakReference<ClassLoader>(jrubyClassLoader));
            }
        }
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(jrubyClassLoader);
            return block.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private static final WeakHashMap<Ruby, WeakReference<ClassLoader>> jrubyClassLoaders = new WeakHashMap<Ruby, WeakReference<ClassLoader>>();

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime" );
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
}
