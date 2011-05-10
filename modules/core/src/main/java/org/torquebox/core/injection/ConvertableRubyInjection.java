package org.torquebox.core.injection;

import org.jruby.Ruby;

public interface ConvertableRubyInjection {

    Object convert(Ruby ruby);

}
