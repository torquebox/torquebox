package org.torquebox.stomp;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.stilts.stomplet.StompletServer;
import org.projectodd.stilts.stomplet.container.SimpleStompletContainer;

public class StompletContainerService implements Service<SimpleStompletContainer> {

    public StompletContainerService() {
    }

    public void addHost(String hostName) {
        this.hostNames.add( hostName );
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.container = new SimpleStompletContainer();
        try {
            this.container.start();
            StompletServer server = this.serverInjector.getValue();
            if (this.hostNames.isEmpty()) {
                server.setDefaultContainer( this.container );
            } else {
                for (String each : this.hostNames) {
                    server.registerVirtualHost( each, this.container );
                }
            }
        } catch (Exception e) {
            context.failed( new StartException( e ) );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            StompletServer server = this.serverInjector.getValue();
            if ( this.hostNames.isEmpty() ) {
                server.setDefaultContainer( null );
            } else {
                for (String each : this.hostNames) {
                    server.registerVirtualHost( each, this.container );
                }
            }
            this.container.stop();
        } catch (Exception e) {
            // ignore
        }

    }

    @Override
    public SimpleStompletContainer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.container;
    }

    public Injector<StompletServer> getStompletServerInjector() {
        return this.serverInjector;
    }

    private SimpleStompletContainer container;
    private InjectedValue<StompletServer> serverInjector = new InjectedValue<StompletServer>();
    private List<String> hostNames = new ArrayList<String>();;

}
