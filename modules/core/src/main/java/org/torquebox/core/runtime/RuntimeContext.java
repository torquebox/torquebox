package org.torquebox.core.runtime;

import org.jruby.Ruby;

public class RuntimeContext {
    
    public static Ruby getCurrentRuntime() {
    	System.err.println(">>>>>>>>>>>>>> LANCE: Get runtime: " + RuntimeContext.runtime.get());
    	System.err.println(">>>>>>>>>>>>>> LANCE: Get runtime thread: " + Thread.currentThread().getName());
       return RuntimeContext.runtime.get(); 
    }

    public static void setCurrentRuntime(Ruby ruby) {
    	System.err.println(">>>>>>>>>>>>>> LANCE: Set runtime: " + ruby);
    	System.err.println(">>>>>>>>>>>>>> LANCE: Set runtime thread: " + Thread.currentThread().getName());
        RuntimeContext.runtime.set( ruby );
    }
    
    private static final ThreadLocal<Ruby> runtime = new ThreadLocal<Ruby>();
}
