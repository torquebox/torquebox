package org.torquebox.interp.core;

import java.util.Set;

public interface BasicRubyRuntimePoolMBean {
    
    String getName();
    Object evaluate(String code) throws Exception;
    
    Set<String> getAllRuntimeNames();

}
