package org.torquebox.core.component;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public interface ComponentInstantiator {
    
    IRubyObject newInstance(Ruby runtime, Object[] initParams);

}
