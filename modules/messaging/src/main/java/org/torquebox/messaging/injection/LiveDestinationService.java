package org.torquebox.messaging.injection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class LiveDestinationService implements Service<LiveDestination> {

    @Override
    public LiveDestination getValue() throws IllegalStateException, IllegalArgumentException {
        return this.liveDestination;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.liveDestination = new LiveDestination( this.connectionFactoryInjector.getValue(), this.destinationInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.liveDestination = null;
    }
    
    public Injector<ConnectionFactory> getConnectionFactoryInjector() {
        return this.connectionFactoryInjector;
    }
    
    public Injector<Destination> getDestinationInjector() {
        return this.destinationInjector;
    }

    private LiveDestination liveDestination;
    private InjectedValue<ConnectionFactory> connectionFactoryInjector = new InjectedValue<ConnectionFactory>();
    private InjectedValue<Destination> destinationInjector = new InjectedValue<Destination>();

}
