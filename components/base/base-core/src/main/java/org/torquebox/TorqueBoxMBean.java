package org.torquebox;

public interface TorqueBoxMBean {

    String getVersion();
    String getRevision();
    String getBuildNumber();
    
    Object evaluate(String script) throws Exception;
    
}
