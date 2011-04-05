package org.torquebox.interp.core;

public interface BasicRubyRuntimePoolMBean {
    
    Object evaluate(String code) throws Exception;

}
