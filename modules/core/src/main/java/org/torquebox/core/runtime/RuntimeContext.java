package org.torquebox.core.runtime;

import java.util.Map;
import java.util.WeakHashMap;

import org.jruby.Ruby;

public class RuntimeContext {

    public static Ruby getCurrentRuntime() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            return null;
        }
        System.err.println( "getting for CL: " + cl );
        return contexts.get( cl );
    }

    public static void setCurrentRuntime(Ruby ruby) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.err.println( "setting for CL: " + cl );
        System.err.println( " -->" + ruby );
        if (cl == null) {
            return;
        }
        contexts.put(  cl, ruby );
    }

    private static final Map<ClassLoader, Ruby> contexts = new WeakHashMap<ClassLoader, Ruby>();
}
