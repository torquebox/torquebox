package org.torquebox.interp.core;

public interface DefaultRubyRuntimePoolMBean extends BasicRubyRuntimePoolMBean {
    
    void setMinimumInstances(int minInstances);
    int getMinimumInstances();

    void setMaximumInstances(int maxInstances);
    int getMaximumInstances();
    
    int getSize();
    int getBorrowed();
    int getAvailable();

}
