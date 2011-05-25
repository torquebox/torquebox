package org.torquebox.messaging;

import javax.jms.Destination;

public interface MessageProcessorGroupMBean {

    void start() throws Exception;

    void stop() throws Exception;

    int getConcurrency();

    String getName();

    Destination getDestination();

    String getDestinationName();

    String getMessageSelector();

    String getStatus();

}
