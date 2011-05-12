package org.torquebox.core.injection.analysis;

import org.jruby.runtime.PositionAware;

public class InjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private PositionAware position;

    public InjectionException(PositionAware position) {
        this.position = position;
    }

    public InjectionException(Throwable rootCause) {
        super( rootCause );
    }

    public PositionAware getPosition() {
        return this.position;
    }

    public String getMessage() {
        StringBuffer msg = new StringBuffer();

        if (this.position != null) {
            msg.append( "Invalid injection: " + this.position );
        }

        if (getCause() != null) {
            msg.append( getCause().getMessage() );
        }

        return msg.toString();
    }

}
