package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public interface RubyComponentResolver {

    IRubyObject resolve(Ruby ruby) throws Exception;

}
