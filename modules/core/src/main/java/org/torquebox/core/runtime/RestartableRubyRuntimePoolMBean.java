package org.torquebox.core.runtime;

public interface RestartableRubyRuntimePoolMBean extends BasicRubyRuntimePoolMBean {

    void restart() throws Exception;

}
