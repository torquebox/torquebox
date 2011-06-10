package org.torquebox.web.websockets;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class WebSocketsServerService implements Service<WebSocketsServer> {

    @Override
    public WebSocketsServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        log.info(  "Starting WebSocketsServer Service"  );
        this.server = new WebSocketsServer( 8081 );
        //this.server.setExecutor( this.executorInjector.getValue() );
        // FIXME: Use domain model to acquire AS-managed Executor
        this.server.setExecutor( Executors.newFixedThreadPool( 10 ) );
        context.asynchronous();
        context.execute( new Runnable() {
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
    
    Injector<Executor> getExecutorInjector() {
        return this.executorInjector;
    }

    private WebSocketsServer server;
    
    private InjectedValue<Executor> executorInjector = new InjectedValue<Executor>();

    private static final Logger log = Logger.getLogger( "org.torquebox.web.websockets" );
}
