package org.torquebox.core;

public interface GlobalRubyMBean {
    
    Object evaluate(String script) throws Exception;
    String evaluateToString(String script) throws Exception;

}
