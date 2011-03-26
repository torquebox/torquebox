package org.torquebox.injection;

import org.jruby.runtime.PositionAware;

public class InjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private PositionAware position;
    
    public InjectionException(PositionAware position) {
        this.position = position;
    }
    
    public PositionAware getPosition() {
        return this.position;
    }
    
    public String getMessage() {
        return "Invalid injection: " + this.position;
    }

}
