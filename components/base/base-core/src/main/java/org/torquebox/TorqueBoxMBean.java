package org.torquebox;

public interface TorqueBoxMBean {

    String getVersion();
    String getRevision();
    String getBuildNumber();
    
    String getGlobalRuntimeName();
    Object evaluate(String script) throws Exception;
    
}
