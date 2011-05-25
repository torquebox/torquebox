package org.torquebox.web.component;

import org.jruby.runtime.builtin.IRubyObject;
import org.torquebox.core.component.AbstractRubyComponent;
import org.torquebox.web.rack.RackEnvironment;
import org.torquebox.web.rack.RackResponse;

public class RackApplicationComponent extends AbstractRubyComponent {

    public RackApplicationComponent() {

    }
    
    public RackApplicationComponent(IRubyObject component) {
        super( component );
    }

    public RackResponse call(RackEnvironment env) {
    	return new RackResponse( (IRubyObject) _callRubyMethod( "call", env.getEnv() ) );
    }

}
