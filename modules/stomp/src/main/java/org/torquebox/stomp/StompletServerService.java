package org.torquebox.stomp;

import javax.transaction.TransactionManager;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
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
        System.err.println( "Starting STOMP server with: " + this.bindingInjector.getValue() );
        try {
            this.server.setTransactionManager( this.transactionManagerInjector.getValue() );
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
    }
    
    public Injector<TransactionManager> getTransactionManagerInjector() {
        return this.transactionManagerInjector;
    }
    
    public Injector<SocketBinding> getBindingInjector() {
        return this.bindingInjector;
    }


    private StompletServer server;
    private InjectedValue<TransactionManager> transactionManagerInjector = new InjectedValue<TransactionManager>();
    private InjectedValue<SocketBinding> bindingInjector = new InjectedValue<SocketBinding>();
    
}
