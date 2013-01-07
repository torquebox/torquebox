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

package org.torquebox.core.runtime;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.jboss.logging.Logger;
import org.jruby.Ruby;
import org.jruby.util.JRubyClassLoader;

public class RuntimeContext {

    public static Ruby getCurrentRuntime() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            if (cl instanceof JRubyClassLoader) {
                WeakReference<Ruby> ref = contexts.get( cl );
                if ( ref == null ) {
                    return null;
                }
                Ruby ruby = ref.get();
                return ruby;
            }
            cl = cl.getParent();
        }

        return null;
    }

    public static void registerRuntime(Ruby ruby) {
        contexts.put( ruby.getJRubyClassLoader(), new WeakReference<Ruby>(ruby) );
        return;
    }
    
    public static void deregisterRuntime(Ruby ruby) {
        contexts.remove( ruby.getJRubyClassLoader() );
    }

    private static final Logger log = Logger.getLogger( "org.torquebox.core.runtime.context" );
    private static final Map<JRubyClassLoader, WeakReference<Ruby>> contexts = Collections.synchronizedMap( new WeakHashMap<JRubyClassLoader, WeakReference<Ruby>>() );
}
