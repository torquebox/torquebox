package org.torquebox.injection;

import org.jruby.runtime.PositionAware;

public class AmbiguousInjectionException extends InjectionException {

    private String injection;

    public AmbiguousInjectionException(PositionAware position, String injection) {
        super( position );
        this.injection = injection;
    }
    
    public String getMessage() {
        return "Ambgiuous injection '" + this.injection + "': " + getPosition();
    }

    private static final long serialVersionUID = 1L;

}
