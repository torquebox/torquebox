package org.torquebox.core.runtime;

import org.jruby.Ruby;

public class ManagedRuntime {
    
    public ManagedRuntime(RubyRuntimePool pool, Ruby runtime) {
        this.pool = pool;
        this.runtime = runtime;
    }
    
    public Ruby getRuby() {
        return this.runtime;
    }
    
    public void release() {
        this.pool.returnRuntime( runtime );
    }
    
    private RubyRuntimePool pool;
    private Ruby runtime;

}
