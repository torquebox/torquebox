package org.torquebox.services;

import java.util.Map;

public class ServiceMetaData {

    
    private String className;
    private Map<String, Object> parameters;
    private boolean requiresSingleton;

    public ServiceMetaData() {
        
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getClassName() { 
        return this.className;
    }
    
    public void setParameters(Map<String,Object> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String,Object> getParameters() {
        return this.parameters;
    }
    
    public void setRequiresSingleton(boolean requiresSingleton) {
        this.requiresSingleton = requiresSingleton;
    }
    
    public boolean isRequiresSingleton() {
        return this.requiresSingleton;
    }
}
