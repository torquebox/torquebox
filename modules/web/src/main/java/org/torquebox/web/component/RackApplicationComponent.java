package org.torquebox.web.component;

import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.component.AbstractRubyComponent;
import org.torquebox.web.rack.RackEnvironment;
import org.torquebox.web.rack.RackResponse;

public class RackApplicationComponent extends AbstractRubyComponent {
    
    public RackApplicationComponent() {
        
    }
    
    public RackResponse call(RackEnvironment env) {
        return new RackResponse( (IRubyObject) __call__( "call", env.getEnv() ) );
    }

}
