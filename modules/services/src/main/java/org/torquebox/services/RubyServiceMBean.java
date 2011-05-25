package org.torquebox.services;

public interface RubyServiceMBean {
    void start() throws Exception;
    void stop() throws Exception;
    
    boolean isStarted();
    boolean isStopped();
    
    String getRubyClassName();
    String getStatus() throws Exception;

}
