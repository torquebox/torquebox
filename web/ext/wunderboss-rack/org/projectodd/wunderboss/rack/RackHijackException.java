package org.projectodd.wunderboss.rack;

public class RackHijackException extends RuntimeException {

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
