package org.torquebox.core.runtime;

import org.jruby.Ruby;

public class RuntimeContext {
    
    public static Ruby getCurrentRuntime() {
       return RuntimeContext.runtime.get(); 
    }

    public static void setCurrentRuntime(Ruby ruby) {
        RuntimeContext.runtime.set( ruby );
    }
    
    public static void clearCurrentRuntime() {
        RuntimeContext.runtime.remove();
    }
    
    private static final ThreadLocal<Ruby> runtime = new ThreadLocal<Ruby>();
}
