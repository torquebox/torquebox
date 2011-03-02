package org.torquebox.services;

public interface RubyServiceProxyMBean {
    
    void start() throws Exception;
    void stop() throws Exception;
    
    boolean isStarted();
    boolean isStopped();
    
    String getRubyClassName();
    String getStatus() throws Exception;

}
