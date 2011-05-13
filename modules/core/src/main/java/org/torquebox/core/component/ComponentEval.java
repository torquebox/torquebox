package org.torquebox.core.component;

import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class ComponentEval implements ComponentInstantiator {
    
    private String code;
    private String location;

    public ComponentEval() {
        
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public IRubyObject newInstance(Ruby runtime, Object[] initParams) {
        return runtime.executeScript( this.code, this.location );
    }
    
    

}
