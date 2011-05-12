package org.torquebox.core.injection.analysis;

import org.jruby.runtime.PositionAware;

public class InvalidInjectionTypeException extends InjectionException {

    private static final long serialVersionUID = 1L;
    
    private String type;

    
    public InvalidInjectionTypeException(PositionAware position, String type) {
        super( position );
        this.type = type;
    }
    
    public String getMessage() {
        return "Invalid injection '" + this.type + "': " + getPosition();
    }

}
