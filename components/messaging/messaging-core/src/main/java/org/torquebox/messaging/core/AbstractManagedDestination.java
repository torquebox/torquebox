package org.torquebox.messaging.core;

import org.hornetq.jms.server.JMSServerManager;
import org.jboss.logging.Logger;

public abstract class AbstractManagedDestination {

    protected Logger log;

    private JMSServerManager server;
    private String name;

    public AbstractManagedDestination() {
        log = Logger.getLogger( getClass() );
    }

    public void setServer(JMSServerManager server) {
        this.server = server;
    }

    public JMSServerManager getServer() {
        return this.server;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract void start() throws Exception;

    public abstract void destroy() throws Exception;
}
