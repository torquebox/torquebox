package org.torquebox.web.websockets;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class WebSocketsServerService implements Service<WebSocketsServer> {

    @Override
    public WebSocketsServer getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        this.server = new WebSocketsServer(8081);
        context.asynchronous();
        context.equals( new Runnable() {
            public void run() {
                server.start();
                context.complete();
            }
        } );

    }

    @Override
    public void stop(StopContext context) {
        this.server.stop();
        this.server = null;
    }

    private WebSocketsServer server;
}
