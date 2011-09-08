package org.torquebox.core.runtime;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.jruby.Ruby;
import org.jruby.util.JRubyClassLoader;

public class RuntimeContext {

    public static Ruby getCurrentRuntime() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            if (cl instanceof JRubyClassLoader) {
                WeakReference<Ruby> ref = contexts.get( cl );
                return ref.get();
            }
            cl = cl.getParent();
        }

        return null;
    }

    public static void registerRuntime(Ruby ruby) {
        contexts.put( ruby.getJRubyClassLoader(), new WeakReference<Ruby>(ruby) );
        return;
    }

    private static final Map<JRubyClassLoader, WeakReference<Ruby>> contexts = new WeakHashMap<JRubyClassLoader, WeakReference<Ruby>>();
}
