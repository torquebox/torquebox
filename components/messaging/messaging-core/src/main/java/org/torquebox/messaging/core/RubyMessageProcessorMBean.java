package org.torquebox.messaging.core;

import javax.jms.Destination;

public interface RubyMessageProcessorMBean {
    
    void start() throws Exception;
    void stop() throws Exception;
    
    int getConcurrency();
    String getName();
    
    Destination getDestination();
    String getMessageSelector();
    

}
