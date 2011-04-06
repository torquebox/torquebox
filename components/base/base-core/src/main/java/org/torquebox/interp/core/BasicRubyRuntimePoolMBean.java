package org.torquebox.interp.core;

public interface BasicRubyRuntimePoolMBean {
    
    String getName();
    Object evaluate(String code) throws Exception;

}
