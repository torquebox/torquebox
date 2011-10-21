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
