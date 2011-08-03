package org.torquebox.stomp;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.projectodd.stilts.stomplet.StompletServer;

public class StompletServerService implements Service<StompletServer> {

    public StompletServerService(StompletServer server) {
        this.server = server;
    }

    @Override
    public StompletServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.server.start();
        } catch (Exception e) {
            context.failed( new StartException( e ) );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.server.stop();
        } catch (Exception e) {
            // ignore, I guess.
        }
        this.server = null;
    }

    private StompletServer server;

}
